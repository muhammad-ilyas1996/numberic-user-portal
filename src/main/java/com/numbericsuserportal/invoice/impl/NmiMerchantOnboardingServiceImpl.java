package com.numbericsuserportal.invoice.impl;

import com.numbericsuserportal.invoice.dto.MerchantNmiConfigDto;
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

        Map<String, Object> payload = buildPayload(userId, request);
        if (!resellerClient.isBoardingConfigured()) {
            app.setStatus(STATUS_PENDING_CONFIGURATION);
            app.setSafeResponseJson("{\"message\":\"Configure nmi.reseller.boarding.url to submit applications to NMI\"}");
            return toDto(onboardingRepo.save(app), "NMI reseller boarding endpoint is not configured yet");
        }

        Map<String, Object> response = resellerClient.submit(payload);
        applyResellerResponse(app, response);
        onboardingRepo.save(app);
        maybeSaveCredentials(userId, app, response);
        return toDto(app, "Onboarding submitted");
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
                onboardingRepo.save(app);
                maybeSaveCredentials(userId, app, response);
            } catch (Exception ignored) {
                // Local status remains useful when reseller status polling is not configured.
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

    private Map<String, Object> buildPayload(Long userId, NmiMerchantOnboardingRequestDto r) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("externalUserId", userId);
        putIfPresent(payload, "businessName", r.getBusinessName());
        putIfPresent(payload, "legalName", r.getLegalName());
        putIfPresent(payload, "dbaName", r.getDbaName());
        putIfPresent(payload, "ein", r.getEin());
        putIfPresent(payload, "businessType", r.getBusinessType());
        putIfPresent(payload, "mcc", r.getMcc());
        putIfPresent(payload, "website", r.getWebsite());
        putIfPresent(payload, "monthlyVolume", r.getMonthlyVolume());
        putIfPresent(payload, "averageTicket", r.getAverageTicket());
        putIfPresent(payload, "highTicket", r.getHighTicket());

        Map<String, Object> contact = new LinkedHashMap<>();
        putIfPresent(contact, "firstName", r.getContactFirstName());
        putIfPresent(contact, "lastName", r.getContactLastName());
        putIfPresent(contact, "email", r.getContactEmail());
        putIfPresent(contact, "phone", r.getContactPhone());
        if (!contact.isEmpty()) payload.put("contact", contact);

        Map<String, Object> address = new LinkedHashMap<>();
        putIfPresent(address, "address1", r.getAddress1());
        putIfPresent(address, "address2", r.getAddress2());
        putIfPresent(address, "city", r.getCity());
        putIfPresent(address, "state", r.getState());
        putIfPresent(address, "zip", r.getZip());
        putIfPresent(address, "country", r.getCountry());
        if (!address.isEmpty()) payload.put("businessAddress", address);

        Map<String, Object> owner = new LinkedHashMap<>();
        putIfPresent(owner, "firstName", r.getOwnerFirstName());
        putIfPresent(owner, "lastName", r.getOwnerLastName());
        putIfPresent(owner, "email", r.getOwnerEmail());
        putIfPresent(owner, "phone", r.getOwnerPhone());
        putIfPresent(owner, "ssnLast4", r.getOwnerSsnLast4());
        putIfPresent(owner, "ownershipPercent", r.getOwnershipPercent());
        if (!owner.isEmpty()) payload.put("owner", owner);

        Map<String, Object> settlement = new LinkedHashMap<>();
        putIfPresent(settlement, "bankName", r.getSettlementBankName());
        putIfPresent(settlement, "routingNumber", r.getSettlementRoutingNumber());
        putIfPresent(settlement, "accountNumber", r.getSettlementAccountNumber());
        putIfPresent(settlement, "accountType", r.getSettlementAccountType());
        if (!settlement.isEmpty()) payload.put("settlementBank", settlement);

        if (r.getResellerPayload() != null && !r.getResellerPayload().isEmpty()) {
            payload.putAll(r.getResellerPayload());
        }
        return payload;
    }

    private void applyResellerResponse(MerchantNmiOnboardingApplication app, Map<String, Object> response) {
        Date now = new Date();
        app.setUpdatedOn(now);
        String applicationId = firstNonBlank(
                findString(response, "applicationId"),
                findString(response, "application_id"),
                findString(response, "boardingApplicationId"),
                findString(response, "id")
        );
        String merchantId = firstNonBlank(
                findString(response, "merchantId"),
                findString(response, "merchant_id"),
                findString(response, "nmiMerchantId"),
                findString(response, "gatewayMerchantId")
        );
        String status = normalizeStatus(firstNonBlank(
                findString(response, "status"),
                findString(response, "boardingStatus"),
                findString(response, "applicationStatus")
        ));
        if (applicationId != null) app.setNmiApplicationId(applicationId);
        if (merchantId != null) app.setNmiMerchantId(merchantId);
        app.setStatus(status != null ? status : STATUS_SUBMITTED);
        if (STATUS_ACTIVE.equals(app.getStatus()) || STATUS_APPROVED.equals(app.getStatus())) {
            app.setApprovedOn(now);
        }
        if (STATUS_DECLINED.equals(app.getStatus())) {
            app.setDeclinedReason(firstNonBlank(findString(response, "declinedReason"), findString(response, "reason"), findString(response, "message")));
        }
        app.setSafeResponseJson(resellerClient.toJson(maskSensitive(response)));
    }

    private void maybeSaveCredentials(Long userId, MerchantNmiOnboardingApplication app, Map<String, Object> response) {
        String securityKey = firstNonBlank(findString(response, "securityKey"), findString(response, "security_key"), findString(response, "apiKey"), findString(response, "api_key"));
        String username = firstNonBlank(findString(response, "username"), findString(response, "gatewayUsername"), findString(response, "nmiUsername"));
        String password = firstNonBlank(findString(response, "password"), findString(response, "gatewayPassword"), findString(response, "nmiPassword"));
        String transactionUrl = firstNonBlank(findString(response, "transactionUrl"), findString(response, "transaction_url"));

        boolean hasCredentials = securityKey != null || (username != null && password != null);
        if (!hasCredentials) {
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
            if (direct != null) return asString(direct);
            for (Object value : map.values()) {
                String found = findString(value, key);
                if (found != null) return found;
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

    private static void putIfPresent(Map<String, Object> map, String key, Object value) {
        if (value instanceof String s) {
            if (!s.isBlank()) map.put(key, s.trim());
        } else if (value != null) {
            map.put(key, value);
        }
    }

    private static boolean isTerminal(String status) {
        return STATUS_ACTIVE.equals(status) || STATUS_APPROVED.equals(status) || STATUS_DECLINED.equals(status);
    }

    private static String normalizeStatus(String status) {
        if (status == null || status.isBlank()) return null;
        String s = status.trim().toUpperCase(Locale.ROOT).replace(' ', '_');
        if (s.contains("APPROV") || s.contains("ACTIVE")) return STATUS_ACTIVE;
        if (s.contains("DECLIN") || s.contains("REJECT")) return STATUS_DECLINED;
        if (s.contains("REVIEW")) return "UNDER_REVIEW";
        if (s.contains("PEND")) return "PENDING";
        return s;
    }

    private static String asString(Object value) {
        return value != null ? String.valueOf(value) : null;
    }

    private static String firstNonBlank(String... values) {
        if (values == null) return null;
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) return value.trim();
        }
        return null;
    }

    private static Long parseLong(Object value) {
        if (value == null) return null;
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (Exception e) {
            return null;
        }
    }
}
