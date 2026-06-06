package com.numbericsuserportal.invoice.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.numbericsuserportal.invoice.dto.NmiBoardingFlowResult;
import com.numbericsuserportal.invoice.dto.NmiMerchantOnboardingRequestDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * NMI Partner v4 merchant boarding client.
 *
 * @see <a href="https://docs.nmi.com/reference/create-merchant.md">Create Merchant</a>
 */
@Service
public class NmiResellerOnboardingClient {

    private static final Pattern PLACEHOLDER = Pattern.compile("\\{([a-zA-Z0-9_]+)}");

    @Value("${nmi.reseller.api-base-url:}")
    private String apiBaseUrl;

    /** Legacy override for create-merchant POST URL. */
    @Value("${nmi.reseller.boarding.url:}")
    private String boardingUrl;

    /** Legacy status URL template with {applicationId} or {gatewayId}. */
    @Value("${nmi.reseller.status.url-template:}")
    private String statusUrlTemplate;

    @Value("${nmi.reseller.api.key:}")
    private String apiKey;

    @Value("${nmi.reseller.username:}")
    private String username;

    @Value("${nmi.reseller.password:}")
    private String password;

    @Value("${nmi.reseller.auth.header-name:Authorization}")
    private String authHeaderName;

    /** NMI v4 expects the raw key in Authorization (no Bearer prefix by default). */
    @Value("${nmi.reseller.auth.header-prefix:}")
    private String authHeaderPrefix;

    @Value("${nmi.reseller.default-merchant-type:gateway}")
    private String defaultMerchantType;

    @Value("${nmi.reseller.default-timezone:America/New_York}")
    private String defaultTimezone;

    @Value("${nmi.reseller.default-language:en_US}")
    private String defaultLanguage;

    @Value("${nmi.reseller.transaction-url:https://secure.networkmerchants.com/api/transact.php}")
    private String defaultTransactionUrl;

    /** JSON template for POST /v4/processors. Placeholders: {gatewayId}, {mcc}, {company}. */
    @Value("${nmi.reseller.processor.payload-template:}")
    private String processorPayloadTemplate;

    /** Semicolon-separated JSON templates for value-added services on /v4/processors. */
    @Value("${nmi.reseller.vas.payload-templates:}")
    private String vasPayloadTemplates;

    @Value("${nmi.reseller.fee-schedule-id:}")
    private String feeScheduleId;

    @Value("${nmi.reseller.agreement-text-id:}")
    private String agreementTextId;

    @Value("${nmi.reseller.auto-complete:false}")
    private boolean autoComplete;

    @Value("${nmi.reseller.auto-create-payment-key:true}")
    private boolean autoCreatePaymentKey;

    @Value("${nmi.reseller.payment-key-permissions:transaction,tokenization}")
    private String paymentKeyPermissions;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper;

    public NmiResellerOnboardingClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public boolean isBoardingConfigured() {
        if (isV4Mode()) {
            return apiKey != null && !apiKey.isBlank();
        }
        return boardingUrl != null && !boardingUrl.isBlank();
    }

    public boolean isV4Mode() {
        return apiBaseUrl != null && !apiBaseUrl.isBlank();
    }

    /**
     * Runs the full NMI v4 boarding flow when api-base-url is configured.
     * Falls back to a single POST for legacy boarding.url setups.
     */
    public NmiBoardingFlowResult runBoardingFlow(NmiMerchantOnboardingRequestDto request, Long userId) {
        if (!isBoardingConfigured()) {
            throw new IllegalStateException("NMI reseller boarding is not configured");
        }
        if (isV4Mode()) {
            return runV4BoardingFlow(request, userId);
        }
        return runLegacyBoardingFlow(buildLegacyPayload(userId, request));
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> fetchStatus(String gatewayId) {
        if (gatewayId == null || gatewayId.isBlank()) {
            throw new IllegalArgumentException("gatewayId is required");
        }
        if (isV4Mode()) {
            return getJson(merchantUrl(gatewayId));
        }
        if (statusUrlTemplate == null || statusUrlTemplate.isBlank()) {
            throw new IllegalStateException("NMI reseller status endpoint is not configured");
        }
        String url = statusUrlTemplate
                .replace("{applicationId}", gatewayId)
                .replace("{gatewayId}", gatewayId);
        return getJson(url);
    }

    public String createPaymentKeyIfConfigured(String gatewayId) {
        if (!autoCreatePaymentKey || gatewayId == null || gatewayId.isBlank() || !isV4Mode()) {
            return null;
        }
        try {
            return createPaymentKey(gatewayId);
        } catch (Exception ignored) {
            return null;
        }
    }

    public String toJson(Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            return "{}";
        }
    }

    private NmiBoardingFlowResult runLegacyBoardingFlow(Map<String, Object> payload) {
        NmiBoardingFlowResult result = new NmiBoardingFlowResult();
        try {
            Map<String, Object> response = postJson(boardingUrl, payload);
            result.setSuccess(true);
            result.addStep("LEGACY_SUBMIT");
            result.putResponse("legacySubmit", response);
            result.setGatewayId(firstNonBlank(
                    asString(response.get("id")),
                    asString(response.get("applicationId")),
                    asString(response.get("merchantId"))
            ));
            result.setStatus(asString(response.get("status")));
            result.setSecurityKey(firstNonBlank(
                    asString(response.get("securityKey")),
                    asString(response.get("keyText"))
            ));
            result.setTransactionUrl(firstNonBlank(
                    asString(response.get("transactionUrl")),
                    defaultTransactionUrl
            ));
            return result;
        } catch (HttpStatusCodeException e) {
            result.setSuccess(false);
            result.setErrorMessage(extractErrorMessage(e));
            return result;
        }
    }

    private NmiBoardingFlowResult runV4BoardingFlow(NmiMerchantOnboardingRequestDto request, Long userId) {
        NmiBoardingFlowResult result = new NmiBoardingFlowResult();
        result.setTransactionUrl(defaultTransactionUrl);
        String gatewayId = null;

        try {
            Map<String, Object> createPayload = buildCreateMerchantPayload(request, userId);
            Map<String, Object> createResponse = postJson(merchantsUrl(), createPayload);
            result.addStep("CREATE_MERCHANT");
            result.putResponse("createMerchant", createResponse);

            gatewayId = asString(createResponse.get("id"));
            result.setGatewayId(gatewayId);
            result.setStatus(asString(createResponse.get("status")));

            if (gatewayId == null || gatewayId.isBlank()) {
                result.setSuccess(false);
                result.setErrorMessage("NMI create merchant response did not include gateway id");
                return result;
            }

            Map<String, Object> requestProcessor = resolveProcessorPayload(request, gatewayId);
            if (requestProcessor != null) {
                Map<String, Object> processorResponse = postJson(processorsUrl(), requestProcessor);
                result.addStep("ADD_PROCESSOR");
                result.putResponse("addProcessor", processorResponse);
            } else if (processorPayloadTemplate != null && !processorPayloadTemplate.isBlank()) {
                Map<String, Object> processorPayload = parseTemplate(processorPayloadTemplate, gatewayId, request);
                Map<String, Object> processorResponse = postJson(processorsUrl(), processorPayload);
                result.addStep("ADD_PROCESSOR");
                result.putResponse("addProcessor", processorResponse);
            }

            for (Map<String, Object> vasPayload : resolveVasPayloads(request, gatewayId)) {
                Map<String, Object> vasResponse = postJson(processorsUrl(), vasPayload);
                String serviceId = asString(vasPayload.get("serviceId"));
                result.addStep("ADD_VAS_" + (serviceId != null ? serviceId.toUpperCase(Locale.ROOT) : "SERVICE"));
                result.putResponse("vas_" + serviceId, vasResponse);
            }

            Map<String, Object> patchBody = buildCompletionPatchBody(request);
            if (!patchBody.isEmpty()) {
                Map<String, Object> patchResponse = patchJson(merchantUrl(gatewayId), patchBody);
                if (patchBody.containsKey("costPlan") && !patchBody.containsKey("status")) {
                    result.addStep("ASSIGN_FEE_SCHEDULE");
                    result.putResponse("assignFeeSchedule", patchResponse);
                } else {
                    result.addStep("COMPLETE_MERCHANT");
                    result.putResponse("completeMerchant", patchResponse);
                }
                result.setStatus(asString(patchResponse.get("status")));
            }

            Map<String, Object> merchant = getJson(merchantUrl(gatewayId));
            result.putResponse("merchant", merchant);
            result.setStatus(firstNonBlank(asString(merchant.get("status")), result.getStatus()));

            if (autoCreatePaymentKey && isActiveStatus(result.getStatus())) {
                String key = createPaymentKey(gatewayId);
                if (key != null) {
                    result.addStep("CREATE_PAYMENT_KEY");
                    result.setSecurityKey(key);
                    Map<String, Object> keyResponse = new LinkedHashMap<>();
                    keyResponse.put("keyText", key);
                    keyResponse.put("securityKey", key);
                    result.putResponse("paymentKey", keyResponse);
                }
            }

            result.setSuccess(true);
            return result;
        } catch (HttpStatusCodeException e) {
            result.setSuccess(false);
            result.setGatewayId(gatewayId);
            result.setErrorMessage(extractErrorMessage(e));
            return result;
        } catch (Exception e) {
            result.setSuccess(false);
            result.setGatewayId(gatewayId);
            result.setErrorMessage(e.getMessage());
            return result;
        }
    }

    private Map<String, Object> buildCreateMerchantPayload(NmiMerchantOnboardingRequestDto r, Long userId) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", defaultMerchantType);
        payload.put("company", firstNonBlank(r.getBusinessName(), r.getLegalName()));
        payload.put("externalIdentifier", "numbrics-user-" + userId);
        payload.put("country", firstNonBlank(r.getCountry(), "US"));
        payload.put("address1", r.getAddress1());
        payload.put("address2", r.getAddress2());
        payload.put("city", r.getCity());
        payload.put("state", r.getState());
        payload.put("zip", r.getZip());
        payload.put("url", firstNonBlank(r.getWebsite(), ""));
        payload.put("timezone", firstNonBlank(r.getTimezone(), defaultTimezone));
        payload.put("firstName", firstNonBlank(r.getContactFirstName(), r.getOwnerFirstName()));
        payload.put("lastName", firstNonBlank(r.getContactLastName(), r.getOwnerLastName()));
        payload.put("email", r.getContactEmail());
        payload.put("phone", r.getContactPhone());
        payload.put("language", firstNonBlank(r.getLanguage(), defaultLanguage));
        payload.put("username", resolveUsername(r, userId));

        String resolvedFeeScheduleId = resolveFeeScheduleId(r);
        if (resolvedFeeScheduleId != null) {
            payload.put("costPlan", parseNumericIfPossible(resolvedFeeScheduleId));
        }

        Map<String, Object> accountInfo = new LinkedHashMap<>();
        accountInfo.put("checkAccount", r.getSettlementAccountNumber());
        accountInfo.put("checkAba", r.getSettlementRoutingNumber());
        accountInfo.put("accountHolderType", firstNonBlank(r.getAccountHolderType(), "business"));
        accountInfo.put("accountType", normalizeAccountType(r.getSettlementAccountType()));
        payload.put("accountInfo", accountInfo);

        if (r.getResellerPayload() != null && !r.getResellerPayload().isEmpty()) {
            payload.putAll(r.getResellerPayload());
        }
        return payload;
    }

    private Map<String, Object> buildCompletionPatchBody(NmiMerchantOnboardingRequestDto request) {
        Map<String, Object> patch = new LinkedHashMap<>();
        String resolvedFeeScheduleId = resolveFeeScheduleId(request);
        if (resolvedFeeScheduleId != null) {
            patch.put("costPlan", parseNumericIfPossible(resolvedFeeScheduleId));
        }
        if (resolveAutoComplete(request)) {
            patch.put("status", "active");
            patch.put("activatePendingServices", true);
            String resolvedAgreementTextId = resolveAgreementTextId(request);
            if (resolvedAgreementTextId != null) {
                patch.put("agreementTextId", resolvedAgreementTextId);
            }
        }
        return patch;
    }

    private Map<String, Object> resolveProcessorPayload(NmiMerchantOnboardingRequestDto request, String gatewayId) {
        if (request.getProcessorPayload() == null || request.getProcessorPayload().isEmpty()) {
            return null;
        }
        return applyGatewayPlaceholders(new LinkedHashMap<>(request.getProcessorPayload()), gatewayId, request);
    }

    private List<Map<String, Object>> resolveVasPayloads(NmiMerchantOnboardingRequestDto request, String gatewayId) {
        List<Map<String, Object>> payloads = new ArrayList<>();
        if (request.getVasPayloads() != null) {
            for (Map<String, Object> vas : request.getVasPayloads()) {
                if (vas != null && !vas.isEmpty()) {
                    payloads.add(applyGatewayPlaceholders(new LinkedHashMap<>(vas), gatewayId, request));
                }
            }
        }
        if (!payloads.isEmpty()) {
            return payloads;
        }
        for (String vasTemplate : splitTemplates(vasPayloadTemplates)) {
            payloads.add(parseTemplate(vasTemplate, gatewayId, request));
        }
        return payloads;
    }

    private String resolveFeeScheduleId(NmiMerchantOnboardingRequestDto request) {
        return firstNonBlank(request.getFeeScheduleId(), feeScheduleId);
    }

    private String resolveAgreementTextId(NmiMerchantOnboardingRequestDto request) {
        return firstNonBlank(request.getAgreementTextId(), agreementTextId);
    }

    private boolean resolveAutoComplete(NmiMerchantOnboardingRequestDto request) {
        if (request.getAutoComplete() != null) {
            return request.getAutoComplete();
        }
        return autoComplete;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> applyGatewayPlaceholders(Map<String, Object> payload, String gatewayId,
                                                         NmiMerchantOnboardingRequestDto request) {
        Map<String, String> values = templateValues(gatewayId, request);
        payload.replaceAll((key, value) -> replacePlaceholders(value, values));
        if (!payload.containsKey("merchantId") || payload.get("merchantId") == null
                || String.valueOf(payload.get("merchantId")).isBlank()) {
            payload.put("merchantId", gatewayId);
        }
        return payload;
    }

    private Object replacePlaceholders(Object value, Map<String, String> values) {
        if (value instanceof String s) {
            String replaced = s;
            for (Map.Entry<String, String> entry : values.entrySet()) {
                replaced = replaced.replace("{" + entry.getKey() + "}", entry.getValue());
            }
            return replaced;
        }
        if (value instanceof Map<?, ?> nested) {
            Map<String, Object> copy = new LinkedHashMap<>();
            nested.forEach((k, v) -> copy.put(String.valueOf(k), replacePlaceholders(v, values)));
            return copy;
        }
        if (value instanceof List<?> list) {
            List<Object> copy = new ArrayList<>();
            for (Object item : list) {
                copy.add(replacePlaceholders(item, values));
            }
            return copy;
        }
        return value;
    }

    private Map<String, Object> buildLegacyPayload(Long userId, NmiMerchantOnboardingRequestDto r) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("externalUserId", userId);
        putIfPresent(payload, "businessName", r.getBusinessName());
        putIfPresent(payload, "legalName", r.getLegalName());
        putIfPresent(payload, "contactEmail", r.getContactEmail());
        if (r.getResellerPayload() != null) {
            payload.putAll(r.getResellerPayload());
        }
        return payload;
    }

    @SuppressWarnings("unchecked")
    private String createPaymentKey(String gatewayId) {
        List<String> permissions = new ArrayList<>();
        for (String part : paymentKeyPermissions.split(",")) {
            if (part != null && !part.trim().isEmpty()) {
                permissions.add(part.trim());
            }
        }
        if (permissions.isEmpty()) {
            permissions = Arrays.asList("transaction", "tokenization");
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("description", "Numbrics invoice payments");
        body.put("permissions", permissions);

        Map<String, Object> response = postJson(securityKeysUrl(gatewayId), body);
        return firstNonBlank(asString(response.get("keyText")), asString(response.get("securityKey")));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseTemplate(String template, String gatewayId, NmiMerchantOnboardingRequestDto request) {
        String rendered = template;
        Map<String, String> values = templateValues(gatewayId, request);
        Matcher matcher = PLACEHOLDER.matcher(template);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String key = matcher.group(1);
            String replacement = values.getOrDefault(key, "");
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);
        rendered = sb.toString();

        try {
            return objectMapper.readValue(rendered, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            throw new IllegalStateException("Invalid NMI payload template JSON: " + e.getMessage(), e);
        }
    }

    private Map<String, String> templateValues(String gatewayId, NmiMerchantOnboardingRequestDto request) {
        Map<String, String> values = new LinkedHashMap<>();
        values.put("gatewayId", gatewayId);
        values.put("merchantId", gatewayId);
        values.put("applicationId", gatewayId);
        values.put("mcc", firstNonBlank(request.getMcc(), "5999"));
        values.put("company", firstNonBlank(request.getBusinessName(), request.getLegalName(), ""));
        return values;
    }

    private List<String> splitTemplates(String templates) {
        List<String> result = new ArrayList<>();
        if (templates == null || templates.isBlank()) {
            return result;
        }
        for (String part : templates.split(";")) {
            if (part != null && !part.trim().isEmpty()) {
                result.add(part.trim());
            }
        }
        return result;
    }

    private Map<String, Object> postJson(String url, Map<String, Object> payload) {
        ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                new HttpEntity<>(payload, headers()),
                Map.class
        );
        return copyBody(response);
    }

    private Map<String, Object> patchJson(String url, Map<String, Object> payload) {
        ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.PATCH,
                new HttpEntity<>(payload, headers()),
                Map.class
        );
        return copyBody(response);
    }

    private Map<String, Object> getJson(String url) {
        ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(headers()),
                Map.class
        );
        return copyBody(response);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> copyBody(ResponseEntity<Map> response) {
        return response.getBody() != null ? new LinkedHashMap<>(response.getBody()) : Map.of();
    }

    private HttpHeaders headers() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        if (apiKey != null && !apiKey.isBlank()) {
            headers.set(authHeaderName, (authHeaderPrefix != null ? authHeaderPrefix : "") + apiKey.trim());
        } else if (username != null && !username.isBlank() && password != null) {
            String basic = Base64.getEncoder()
                    .encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
            headers.set(HttpHeaders.AUTHORIZATION, "Basic " + basic);
        }
        return headers;
    }

    private String merchantsUrl() {
        if (boardingUrl != null && !boardingUrl.isBlank()) {
            return boardingUrl.trim();
        }
        return trimSlash(apiBaseUrl) + "/v4/merchants";
    }

    private String merchantUrl(String gatewayId) {
        if (statusUrlTemplate != null && !statusUrlTemplate.isBlank()) {
            return statusUrlTemplate
                    .replace("{applicationId}", gatewayId)
                    .replace("{gatewayId}", gatewayId);
        }
        return trimSlash(apiBaseUrl) + "/v4/merchants/" + gatewayId;
    }

    private String processorsUrl() {
        return trimSlash(apiBaseUrl) + "/v4/processors";
    }

    private String securityKeysUrl(String gatewayId) {
        return trimSlash(apiBaseUrl) + "/v4/merchants/" + gatewayId + "/security_keys";
    }

    private static String resolveUsername(NmiMerchantOnboardingRequestDto request, Long userId) {
        if (request.getUsername() != null && !request.getUsername().isBlank()) {
            return sanitizeUsername(request.getUsername());
        }
        String email = request.getContactEmail();
        if (email != null && email.contains("@")) {
            return sanitizeUsername(email.substring(0, email.indexOf('@')));
        }
        return "numbrics" + userId;
    }

    private static String sanitizeUsername(String value) {
        String cleaned = value.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9._-]", "");
        if (cleaned.isBlank()) {
            return "numbricsmerchant";
        }
        return cleaned.length() > 32 ? cleaned.substring(0, 32) : cleaned;
    }

    private static String normalizeAccountType(String accountType) {
        if (accountType == null || accountType.isBlank()) {
            return "checking";
        }
        String normalized = accountType.trim().toLowerCase(Locale.ROOT);
        if (normalized.startsWith("save")) {
            return "savings";
        }
        return "checking";
    }

    private static Object parseNumericIfPossible(String value) {
        try {
            if (value.contains(".")) {
                return Double.parseDouble(value);
            }
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return value;
        }
    }

    private static boolean isActiveStatus(String status) {
        if (status == null) {
            return false;
        }
        String normalized = status.trim().toLowerCase(Locale.ROOT);
        return normalized.contains("active") || normalized.contains("approved");
    }

    private String extractErrorMessage(HttpStatusCodeException e) {
        String body = e.getResponseBodyAsString();
        if (body != null && !body.isBlank()) {
            try {
                Map<String, Object> parsed = objectMapper.readValue(body, new TypeReference<Map<String, Object>>() {});
                String message = firstNonBlank(
                        asString(parsed.get("message")),
                        asString(parsed.get("error")),
                        asString(parsed.get("detail"))
                );
                if (message != null) {
                    return message;
                }
            } catch (Exception ignored) {
                return body;
            }
            return body;
        }
        return e.getStatusCode() + " " + e.getStatusText();
    }

    private static void putIfPresent(Map<String, Object> map, String key, Object value) {
        if (value instanceof String s) {
            if (!s.isBlank()) {
                map.put(key, s.trim());
            }
        } else if (value != null) {
            map.put(key, value);
        }
    }

    private static String trimSlash(String url) {
        if (url == null) {
            return "";
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    private static String asString(Object value) {
        return value != null ? String.valueOf(value) : null;
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        return null;
    }
}
