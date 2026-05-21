package com.numbericsuserportal.invoice.controller;

import com.numbericsuserportal.invoice.dto.NmiMerchantOnboardingRequestDto;
import com.numbericsuserportal.invoice.dto.NmiMerchantOnboardingStatusDto;
import com.numbericsuserportal.invoice.dto.NmiMerchantDashboardDto;
import com.numbericsuserportal.invoice.dto.MerchantNmiConfigDto;
import com.numbericsuserportal.invoice.service.MerchantNmiConfigService;
import com.numbericsuserportal.invoice.service.NmiMerchantOnboardingService;
import com.numbericsuserportal.usermanagement.domain.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Value;

import java.util.Map;

@RestController
@RequestMapping
public class NmiMerchantOnboardingController {

    private final NmiMerchantOnboardingService onboardingService;
    private final MerchantNmiConfigService merchantNmiConfigService;

    @Value("${nmi.webhook.secret:}")
    private String webhookSecret;

    public NmiMerchantOnboardingController(NmiMerchantOnboardingService onboardingService,
                                           MerchantNmiConfigService merchantNmiConfigService) {
        this.onboardingService = onboardingService;
        this.merchantNmiConfigService = merchantNmiConfigService;
    }

    @PostMapping("/v1/merchant/nmi/onboarding")
    public ResponseEntity<NmiMerchantOnboardingStatusDto> submit(
            @RequestBody NmiMerchantOnboardingRequestDto request,
            @AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(onboardingService.submit(currentUser.getUserId(), request));
    }

    @GetMapping("/v1/merchant/nmi/onboarding/status")
    public ResponseEntity<NmiMerchantOnboardingStatusDto> status(@AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(onboardingService.getLatestStatus(currentUser.getUserId()));
    }

    @GetMapping("/v1/merchant/nmi/dashboard")
    public ResponseEntity<NmiMerchantDashboardDto> dashboard(@AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        MerchantNmiConfigDto config = merchantNmiConfigService.getByUserId(currentUser.getUserId());
        NmiMerchantOnboardingStatusDto onboarding = onboardingService.getLatestStatus(currentUser.getUserId());

        NmiMerchantDashboardDto dto = new NmiMerchantDashboardDto();
        dto.setUserId(currentUser.getUserId());
        dto.setNmiConfig(config);
        dto.setOnboarding(onboarding);

        boolean configured = Boolean.TRUE.equals(config.getConfigured());
        String status = config.getBoardingStatus() != null ? config.getBoardingStatus() : onboarding.getStatus();
        boolean active = status == null || status.isBlank()
                ? configured
                : "ACTIVE".equalsIgnoreCase(status) || "APPROVED".equalsIgnoreCase(status);
        dto.setPaymentReady(configured && active);
        if (!configured) {
            dto.setNextAction("SUBMIT_ONBOARDING");
            dto.setMessage("Complete NMI merchant onboarding before collecting invoice payments.");
        } else if (!active) {
            dto.setNextAction("WAIT_FOR_APPROVAL");
            dto.setMessage("NMI merchant account is not active yet. Current status: " + status);
        } else {
            dto.setNextAction("READY_TO_COLLECT");
            dto.setMessage("NMI merchant account is ready for invoice payments.");
        }
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/webhooks/nmi/onboarding")
    public ResponseEntity<NmiMerchantOnboardingStatusDto> webhook(
            @RequestBody Map<String, Object> payload,
            @RequestHeader(value = "X-NMI-Webhook-Secret", required = false) String secretHeader) {
        if (webhookSecret != null && !webhookSecret.isBlank()) {
            Object bodySecret = payload != null ? payload.get("webhook_secret") : null;
            String provided = secretHeader != null && !secretHeader.isBlank()
                    ? secretHeader
                    : bodySecret != null ? String.valueOf(bodySecret) : null;
            if (!webhookSecret.equals(provided)) {
                return ResponseEntity.status(401).build();
            }
        }
        return ResponseEntity.ok(onboardingService.handleWebhook(payload));
    }
}
