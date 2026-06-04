package com.numbericsuserportal.invoice.impl;

import com.numbericsuserportal.invoice.dto.MerchantNmiConfigDto;
import com.numbericsuserportal.invoice.dto.NmiBoardingFlowResult;
import com.numbericsuserportal.invoice.dto.NmiMerchantOnboardingRequestDto;
import com.numbericsuserportal.invoice.dto.NmiMerchantOnboardingStatusDto;
import com.numbericsuserportal.invoice.entity.MerchantNmiConfig;
import com.numbericsuserportal.invoice.entity.MerchantNmiOnboardingApplication;
import com.numbericsuserportal.invoice.repo.MerchantNmiOnboardingApplicationRepo;
import com.numbericsuserportal.invoice.service.MerchantNmiConfigService;
import com.numbericsuserportal.invoice.service.NmiMerchantOnboardingService;
import com.numbericsuserportal.invoice.service.NmiResellerOnboardingClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Service
public class NmiMerchantOnboardingServiceImpl implements NmiMerchantOnboardingService {

    private static final String STATUS_DRAFT = "DRAFT";
    private static final String STATUS_PENDING_CONFIGURATION = "PENDING_RESELLER_CONFIGURATION";
    private static final String STATUS_SUBMITTED = "SUBMITTED";
    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_APPROVED = "APPROVED";
    private static final String STATUS_DECLINED = "DECLINED";
    private static final String STATUS_PENDING = "PENDING";

    private final MerchantNmiOnboardingApplicationRepo onboardingRepo;
    private final MerchantNmiConfigService merchantNmiConfigService;
    private final NmiResellerOnboardingClient resellerClient;

    public NmiMerchantOnboardingServiceImpl(MerchantNmiOnboardingApplicationRepo onboardingRepo,
                                            MerchantNmiConfigService merchantNmiConfigService,
                                            NmiResellerOnboardingClient resellerClient) {
        this.onboardingRepo = onboardingRepo;
        this.merchantNmiConfigService = merchantNmiConfigService;
        this.resellerClient = resellerClient;
    }

    @Override
    @Transactional
    public NmiMerchantOnboardingStatusDto submit(Long userId, NmiMerchantOnboardingRequestDto request) {
        if (userId == null) {
            throw new IllegalArgumentException("Authenticated user is required");
        }
        if (request == null) {
            throw new IllegalArgumentException("Onboarding request is required");
        }

        MerchantNmiOnboardingApplication app = onboardingRepo.findFirstByUserIdOrderByIdDesc(userId)
                .filter(existing -> !isTerminal(existing.getStatus()))
                .orElseGet(MerchantNmiOnboardingApplication::new);

        Date now = new Date();
        if (app.getId() == null) {
            app.setCreatedOn(now);
        }
        app.setUserId(userId);
        app.setUpdatedOn(now);
        app.setSubmittedOn(now);
        app.setBusinessName(firstNonBlank(request.getBusinessName(), request.getLegalName()));
        app.setLegalName(request.getLegalName());
        app.setContactEmail(request.getContactEmail());

        if (!resellerClient.isBoardingConfigured()) {
            app.setStatus(STATUS_PENDING_CONFIGURATION);
            app.setSafeResponseJson("{\"message\":\"Configure nmi.reseller.api-base-url and nmi.reseller.api.key (Partner v4 API key)\"}");
            return toDto(onboardingRepo.save(app), configurationMessage());
        }

        validateRequiredFields(request);

        NmiBoardingFlowResult flowResult = resellerClient.runBoardingFlow(request, userId);
        applyFlowResult(app, flowResult);
        onboardingRepo.save(app);

        Map<String, Object> credentialSource = buildCredentialSource(flowResult);
        maybeSaveCredentials(userId, app, credentialSource);

        if (flowResult.isSuccess()) {
            return toDto(app, buildSuccessMessage(flowResult));
        }
        return toDto(app, firstNonBlank(flowResult.getErrorMessage(), "NMI onboarding failed"));
    }

    @Override
    @Transactional
    public NmiMerchantOnboardingStatusDto getLatestStatus(Long userId) {
        Optional<MerchantNmiOnboardingApplication> appOpt = onboardingRepo.findFirstByUserIdOrderByIdDesc(userId);
        if (appOpt.isEmpty()) {
            NmiMerchantOnboardingStatusDto dto = new NmiMerchantOnboardingStatusDto();
            dto.setUserId(userId);
            dto.setStatus(STATUS_DRAFT);
            dto.setMessage("No NMI onboarding application found");
            dto.setCredentialsConfigured(false);
            return dto;
        }

        MerchantNmiOnboardingApplication app = appOpt.get();
        if (app.getNmiApplicationId() != null && !app.getNmiApplicationId().isBlank()) {
            try {
                Map<String, Object> response = resellerClient.fetchStatus(app.getNmiApplicationId());
                applyResellerResponse(app, response);

                String paymentKey = resellerClient.createPaymentKeyIfConfigured(app.getNmiApplicationId());
                if (paymentKey != null) {
                    Map<String, Object> keyResponse = new LinkedHashMap<>();
                    keyResponse.put("keyText", paymentKey);
                    keyResponse.put("securityKey", paymentKey);
                    keyResponse.put("id", app.getNmiApplicationId());
                    keyResponse.put("status", response.get("status"));
                    maybeSaveCredentials(userId, app, keyResponse);
                } else {
                    maybeSaveCredentials(userId, app, response);
                }

                onboardingRepo.save(app);
            } catch (Exception ignored) {
                // Local status remains useful when reseller status polling fails.
            }
        }
        return toDto(app, null);
    }

    @Override
    @Transactional
    public NmiMerchantOnboardingStatusDto handleWebhook(Map<String, Object> payload) {
        String applicationId = firstNonBlank(
                asString(payload.get("applicationId")),
                asString(payload.get("application_id")),
                asString(payload.get("boardingApplicationId")),
                asString(payload.get("gateway_id")),
                asString(payload.get("id"))
        );
        MerchantNmiOnboardingApplication app = null;
        if (applicationId != null) {
            app = onboardingRepo.findFirstByNmiApplicationIdOrderByIdDesc(applicationId).orElse(null);
        }
        if (app == null) {
            Long userId = parseLong(payload.get("userId"));
            app = userId != null
                    ? onboardingRepo.findFirstByUserIdOrderByIdDesc(userId).orElse(new MerchantNmiOnboardingApplication())
                    : new MerchantNmiOnboardingApplication();
            if (app.getId() == null) {
                app.setCreatedOn(new Date());
            }
            if (userId != null) {
                app.setUserId(userId);
            }
        }
        applyResellerResponse(app, payload);
        app.setUpdatedOn(new Date());
        onboardingRepo.save(app);
        if (app.getUserId() != null) {
            maybeSaveCredentials(app.getUserId(), app, payload);
        }
        return toDto(app, "Webhook processed");
    }

    private void validateRequiredFields(NmiMerchantOnboardingRequestDto request) {
        if (firstNonBlank(request.getBusinessName(), request.getLegalName()) == null) {
            throw new IllegalArgumentException("businessName or legalName is required");
        }
        if (isBlank(request.getContactEmail())) {
            throw new IllegalArgumentException("contactEmail is required");
        }
        if (isBlank(request.getContactPhone())) {
            throw new IllegalArgumentException("contactPhone is required");
        }
        if (isBlank(request.getAddress1()) || isBlank(request.getCity()) || isBlank(request.getState()) || isBlank(request.getZip())) {
            throw new IllegalArgumentException("address1, city, state, and zip are required");
        }
        if (isBlank(request.getSettlementRoutingNumber()) || isBlank(request.getSettlementAccountNumber())) {
            throw new IllegalArgumentException("settlementRoutingNumber and settlementAccountNumber are required");
        }
        if (firstNonBlank(request.getContactFirstName(), request.getOwnerFirstName()) == null) {
            throw new IllegalArgumentException("contactFirstName or ownerFirstName is required");
        }
        if (firstNonBlank(request.getContactLastName(), request.getOwnerLastName()) == null) {
            throw new IllegalArgumentException("contactLastName or ownerLastName is required");
        }
    }

    private void applyFlowResult(MerchantNmiOnboardingApplication app, NmiBoardingFlowResult flowResult) {
        Map<String, Object> combined = new LinkedHashMap<>();
        combined.put("success", flowResult.isSuccess());
        combined.put("stepsCompleted", flowResult.getStepsCompleted());
        combined.put("lastCompletedStep", flowResult.getLastCompletedStep());
        combined.put("errorMessage", flowResult.getErrorMessage());
        combined.put("responses", flowResult.getResponses());

        if (flowResult.getGatewayId() != null) {
            combined.put("id", flowResult.getGatewayId());
        }
        if (flowResult.getStatus() != null) {
            combined.put("status", flowResult.getStatus());
        }
        if (flowResult.getSecurityKey() != null) {
            combined.put("keyText", flowResult.getSecurityKey());
            combined.put("securityKey", flowResult.getSecurityKey());
        }
        if (flowResult.getTransactionUrl() != null) {
            combined.put("transactionUrl", flowResult.getTransactionUrl());
        }

        applyResellerResponse(app, combined);

        if (!flowResult.isSuccess()) {
            if (flowResult.getGatewayId() != null) {
                app.setStatus(STATUS_PENDING);
            } else {
                app.setStatus(STATUS_DECLINED);
            }
            app.setDeclinedReason(flowResult.getErrorMessage());
        }
    }

    private Map<String, Object> buildCredentialSource(NmiBoardingFlowResult flowResult) {
        Map<String, Object> source = new LinkedHashMap<>();
        if (flowResult.getGatewayId() != null) {
            source.put("id", flowResult.getGatewayId());
        }
        if (flowResult.getStatus() != null) {
            source.put("status", flowResult.getStatus());
        }
        if (flowResult.getSecurityKey() != null) {
            source.put("securityKey", flowResult.getSecurityKey());
            source.put("keyText", flowResult.getSecurityKey());
        }
        if (flowResult.getTransactionUrl() != null) {
            source.put("transactionUrl", flowResult.getTransactionUrl());
        }
        source.putAll(flowResult.getResponses());
        return source;
    }

    private String buildSuccessMessage(NmiBoardingFlowResult flowResult) {
        if (flowResult.getSecurityKey() != null) {
            return "NMI merchant onboarded and payment key configured";
        }
        if (STATUS_PENDING.equalsIgnoreCase(firstNonBlank(flowResult.getStatus(), ""))) {
            return "NMI merchant created and pending approval";
        }
        return "NMI onboarding submitted";
    }

    private String configurationMessage() {
        if (resellerClient.isV4Mode()) {
            return "NMI Partner API key is not configured yet";
        }
        return "NMI reseller boarding endpoint is not configured yet";
    }

    private void applyResellerResponse(MerchantNmiOnboardingApplication app, Map<String, Object> response) {
        Date now = new Date();
        app.setUpdatedOn(now);
        String applicationId = firstNonBlank(
                findString(response, "applicationId"),
                findString(response, "application_id"),
                findString(response, "boardingApplicationId"),
                findString(response, "gateway_id"),
                findString(response, "id")
        );
        String merchantId = firstNonBlank(
                findString(response, "merchantId"),
                findString(response, "merchant_id"),
                findString(response, "nmiMerchantId"),
                findString(response, "gatewayMerchantId"),
                applicationId
        );
        String status = normalizeStatus(firstNonBlank(
                findString(response, "status"),
                findString(response, "boardingStatus"),
                findString(response, "applicationStatus")
        ));
        if (applicationId != null) {
            app.setNmiApplicationId(applicationId);
        }
        if (merchantId != null) {
            app.setNmiMerchantId(merchantId);
        }
        app.setStatus(status != null ? status : STATUS_SUBMITTED);
        if (STATUS_ACTIVE.equals(app.getStatus()) || STATUS_APPROVED.equals(app.getStatus())) {
            app.setApprovedOn(now);
        }
        if (STATUS_DECLINED.equals(app.getStatus())) {
            app.setDeclinedReason(firstNonBlank(
                    findString(response, "declinedReason"),
                    findString(response, "reason"),
                    findString(response, "errorMessage"),
                    findString(response, "message")
            ));
        }
        app.setSafeResponseJson(resellerClient.toJson(maskSensitive(response)));
    }

    private void maybeSaveCredentials(Long userId, MerchantNmiOnboardingApplication app, Map<String, Object> response) {
        String securityKey = firstNonBlank(
                findString(response, "securityKey"),
                findString(response, "security_key"),
                findString(response, "apiKey"),
                findString(response, "api_key"),
                findString(response, "keyText")
        );
        String username = firstNonBlank(
                findString(response, "username"),
                findString(response, "gatewayUsername"),
                findString(response, "nmiUsername")
        );
        String password = firstNonBlank(
                findString(response, "password"),
                findString(response, "gatewayPassword"),
                findString(response, "nmiPassword")
        );
        String transactionUrl = firstNonBlank(
                findString(response, "transactionUrl"),
                findString(response, "transaction_url")
        );

        boolean hasCredentials = securityKey != null || (username != null && password != null);
        if (!hasCredentials) {
            updateExistingConfigStatus(userId, app);
            return;
        }

        if (!isMerchantReadyStatus(app.getStatus())) {
            updateExistingConfigStatus(userId, app);
            return;
        }

        MerchantNmiConfigDto dto = new MerchantNmiConfigDto();
        dto.setAuthMethod(securityKey != null ? "api_key" : "username_password");
        dto.setSecurityKey(securityKey);
        dto.setNmiUsername(username);
        dto.setNmiPassword(password);
        dto.setTransactionUrl(transactionUrl);
        dto.setNmiMerchantId(app.getNmiMerchantId());
        dto.setBoardingApplicationId(app.getNmiApplicationId());
        dto.setBoardingStatus(app.getStatus());
        merchantNmiConfigService.save(userId, dto);
    }

    private void updateExistingConfigStatus(Long userId, MerchantNmiOnboardingApplication app) {
        Optional<MerchantNmiConfig> existing = merchantNmiConfigService.getEntityByUserId(userId);
        if (existing.isEmpty()) {
            return;
        }
        MerchantNmiConfig c = existing.get();
        MerchantNmiConfigDto dto = new MerchantNmiConfigDto();
        dto.setAuthMethod(c.getAuthMethod());
        dto.setSecurityKey(c.getSecurityKey());
        dto.setNmiUsername(c.getNmiUsername());
        dto.setNmiPassword(c.getNmiPassword());
        dto.setTransactionUrl(c.getTransactionUrl());
        dto.setNmiMerchantId(firstNonBlank(app.getNmiMerchantId(), c.getNmiMerchantId()));
        dto.setBoardingApplicationId(firstNonBlank(app.getNmiApplicationId(), c.getBoardingApplicationId()));
        dto.setBoardingStatus(app.getStatus());
        merchantNmiConfigService.save(userId, dto);
    }

    private NmiMerchantOnboardingStatusDto toDto(MerchantNmiOnboardingApplication app, String message) {
        NmiMerchantOnboardingStatusDto dto = new NmiMerchantOnboardingStatusDto();
        dto.setId(app.getId());
        dto.setUserId(app.getUserId());
        dto.setStatus(app.getStatus());
        dto.setBusinessName(app.getBusinessName());
        dto.setLegalName(app.getLegalName());
        dto.setContactEmail(app.getContactEmail());
        dto.setNmiApplicationId(app.getNmiApplicationId());
        dto.setNmiMerchantId(app.getNmiMerchantId());
        dto.setDeclinedReason(app.getDeclinedReason());
        dto.setCreatedOn(app.getCreatedOn());
        dto.setUpdatedOn(app.getUpdatedOn());
        dto.setSubmittedOn(app.getSubmittedOn());
        dto.setApprovedOn(app.getApprovedOn());
        dto.setMessage(message);
        Optional<MerchantNmiConfig> config = app.getUserId() != null
                ? merchantNmiConfigService.getEntityByUserId(app.getUserId())
                : Optional.empty();
        dto.setCredentialsConfigured(config.map(c ->
                (c.getSecurityKey() != null && !c.getSecurityKey().isBlank())
                        || (c.getNmiUsername() != null && !c.getNmiUsername().isBlank()
                        && c.getNmiPassword() != null && !c.getNmiPassword().isBlank())
        ).orElse(false));
        return dto;
    }

    private String findString(Object source, String key) {
        if (source instanceof Map<?, ?> map) {
            Object direct = map.get(key);
            if (direct != null) {
                return asString(direct);
            }
            for (Object value : map.values()) {
                String found = findString(value, key);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private Map<String, Object> maskSensitive(Map<String, Object> source) {
        Map<String, Object> masked = new LinkedHashMap<>();
        source.forEach((key, value) -> {
            String lower = key.toLowerCase(Locale.ROOT);
            if (lower.contains("password") || lower.contains("key") || lower.contains("secret")
                    || lower.contains("account") || lower.contains("routing") || lower.contains("ssn")) {
                masked.put(key, "****");
            } else if (value instanceof Map<?, ?> nested) {
                Map<String, Object> nestedMap = new LinkedHashMap<>();
                nested.forEach((nestedKey, nestedValue) -> nestedMap.put(String.valueOf(nestedKey), nestedValue));
                masked.put(key, maskSensitive(nestedMap));
            } else {
                masked.put(key, value);
            }
        });
        return masked;
    }

    private static boolean isTerminal(String status) {
        return STATUS_ACTIVE.equals(status) || STATUS_APPROVED.equals(status) || STATUS_DECLINED.equals(status);
    }

    private static boolean isMerchantReadyStatus(String status) {
        if (status == null || status.isBlank()) {
            return false;
        }
        String normalized = status.trim().toUpperCase(Locale.ROOT);
        return STATUS_ACTIVE.equals(normalized) || STATUS_APPROVED.equals(normalized);
    }

    private static String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        String s = status.trim().toUpperCase(Locale.ROOT).replace(' ', '_');
        if (s.contains("APPROV") || s.contains("ACTIVE")) {
            return STATUS_ACTIVE;
        }
        if (s.contains("DECLIN") || s.contains("REJECT")) {
            return STATUS_DECLINED;
        }
        if (s.contains("REVIEW")) {
            return "UNDER_REVIEW";
        }
        if (s.contains("PEND")) {
            return STATUS_PENDING;
        }
        return s;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
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

    private static Long parseLong(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (Exception e) {
            return null;
        }
    }
}
