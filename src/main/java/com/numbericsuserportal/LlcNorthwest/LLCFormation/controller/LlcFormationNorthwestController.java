package com.numbericsuserportal.LlcNorthwest.LLCFormation.controller;

import com.numbericsuserportal.LlcNorthwest.LLCFormation.dto.NorthwestPrepareResponseDTO;
import com.numbericsuserportal.LlcNorthwest.LLCFormation.entity.LlcFormation;
import com.numbericsuserportal.LlcNorthwest.LLCFormation.repo.LlcFormationRepository;
import com.numbericsuserportal.LlcNorthwest.LLCFormation.service.LlcFormationNorthwestIntegrationService;
import com.numbericsuserportal.LlcNorthwest.LLCFormation.service.LlcFormationNorthwestShoppingCartService;
import com.numbericsuserportal.usermanagement.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/llc-northwest/llc-formation")
@CrossOrigin(origins = "*")
public class LlcFormationNorthwestController {

    @Autowired
    private LlcFormationRepository formationRepository;

    @Autowired
    private LlcFormationNorthwestIntegrationService northwestIntegrationService;

    @Autowired
    private LlcFormationNorthwestShoppingCartService northwestShoppingCartService;

    /**
     * Creates NW company, resolves filing product/method, builds shopping cart JSON.
     * Call after steps 1–2 (state + LLC name) and before payment.
     */
    @PostMapping("/{formationId}/northwest/prepare")
    public ResponseEntity<?> prepare(@AuthenticationPrincipal User currentUser, @PathVariable Long formationId) {
        if (currentUser == null) return ResponseEntity.status(401).build();
        try {
            LlcFormation formation = formationRepository.findByIdAndUserId(formationId, currentUser.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("Formation not found"));
            NorthwestPrepareResponseDTO response = northwestIntegrationService.prepare(formation, currentUser);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Dev/manual fallback when Stripe webhook is unavailable (localhost).
     * Ensures NW integration is ready, then runs shopping cart + checkout.
     */
    @PostMapping("/{formationId}/northwest/submit-after-payment")
    public ResponseEntity<?> submitAfterPayment(@AuthenticationPrincipal User currentUser,
                                                  @PathVariable Long formationId) {
        if (currentUser == null) return ResponseEntity.status(401).build();
        try {
            LlcFormation formation = formationRepository.findByIdAndUserId(formationId, currentUser.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("Formation not found"));
            northwestIntegrationService.ensureReadyForSubmit(formation, currentUser);
            formationRepository.findById(formationId).ifPresent(fresh ->
                    northwestShoppingCartService.submitAfterPaymentIfNeeded(fresh));
            LlcFormation updated = formationRepository.findById(formationId)
                    .orElseThrow(() -> new IllegalArgumentException("Formation not found"));
            return ResponseEntity.ok(Map.of(
                    "formationId", updated.getId(),
                    "status", updated.getStatus(),
                    "filingStatus", updated.getFilingStatus() != null ? updated.getFilingStatus() : "",
                    "companyId", updated.getCompanyId() != null ? updated.getCompanyId() : "",
                    "northwestCheckoutCompletedAt", updated.getNorthwestCheckoutCompletedAt() != null
                            ? updated.getNorthwestCheckoutCompletedAt().toString() : ""
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
