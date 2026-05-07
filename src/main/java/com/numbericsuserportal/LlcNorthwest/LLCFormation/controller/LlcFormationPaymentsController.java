package com.numbericsuserportal.LlcNorthwest.LLCFormation.controller;

import com.numbericsuserportal.LlcNorthwest.LLCFormation.dto.CalculatePaymentResponseDTO;
import com.numbericsuserportal.LlcNorthwest.LLCFormation.dto.CreatePaymentIntentResponseDTO;
import com.numbericsuserportal.LlcNorthwest.LLCFormation.entity.LlcFormation;
import com.numbericsuserportal.LlcNorthwest.LLCFormation.repo.LlcFormationRepository;
import com.numbericsuserportal.LlcNorthwest.LLCFormation.service.LlcFormationPricingService;
import com.numbericsuserportal.usermanagement.domain.User;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/llc-northwest/llc-formation")
@CrossOrigin(origins = "*")
public class LlcFormationPaymentsController {

    @Autowired
    private LlcFormationRepository formationRepository;

    @Autowired
    private LlcFormationPricingService pricingService;

    @PostMapping("/{formationId}/payments/calculate")
    public ResponseEntity<?> calculate(@AuthenticationPrincipal User currentUser, @PathVariable Long formationId) {
        if (currentUser == null) return ResponseEntity.status(401).build();
        try {
            LlcFormation f = formationRepository.findByIdAndUserId(formationId, currentUser.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("Formation not found"));
            CalculatePaymentResponseDTO calc = pricingService.calculate(f);
            pricingService.applySnapshotToFormation(f, calc);
            formationRepository.save(f);
            return ResponseEntity.ok(calc);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{formationId}/payments/intent")
    public ResponseEntity<?> createIntent(@AuthenticationPrincipal User currentUser, @PathVariable Long formationId) {
        if (currentUser == null) return ResponseEntity.status(401).build();
        try {
            LlcFormation f = formationRepository.findByIdAndUserId(formationId, currentUser.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("Formation not found"));

            CalculatePaymentResponseDTO calc = pricingService.calculate(f);
            pricingService.applySnapshotToFormation(f, calc);

            // Stripe amount in cents. client never sends amount.
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(calc.getTotalCents().longValue())
                    .setCurrency("usd")
                    .setDescription("Numbrics LLC formation checkout")
                    .putMetadata("formationId", String.valueOf(formationId))
                    .putMetadata("userId", String.valueOf(currentUser.getUserId()))
                    .build();

            PaymentIntent intent = PaymentIntent.create(params);

            f.setStripePaymentIntentId(intent.getId());
            f.setUpdatedAt(OffsetDateTime.now());
            formationRepository.save(f);

            return ResponseEntity.ok(new CreatePaymentIntentResponseDTO(intent.getClientSecret()));
        } catch (StripeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Stripe error: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}

