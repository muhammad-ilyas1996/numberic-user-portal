package com.numbericsuserportal.LlcNorthwest.LLCFormation.controller;

import com.numbericsuserportal.LlcNorthwest.LLCFormation.entity.LlcFormation;
import com.numbericsuserportal.LlcNorthwest.LLCFormation.repo.LlcFormationRepository;
import com.numbericsuserportal.LlcNorthwest.LLCFormation.service.LlcFormationNorthwestShoppingCartService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.Map;

@RestController
@RequestMapping("/webhooks")
@CrossOrigin(origins = "*")
public class StripeWebhookController {

    @Autowired
    private LlcFormationRepository formationRepository;

    @Autowired
    private LlcFormationNorthwestShoppingCartService llcFormationNorthwestShoppingCartService;

    @Value("${stripe.webhook.secret:}")
    private String stripeWebhookSecret;

    @PostMapping("/stripe")
    public ResponseEntity<?> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "Stripe-Signature", required = false) String sigHeader) {
        try {
            Event event;
            if (stripeWebhookSecret != null && !stripeWebhookSecret.isBlank() && sigHeader != null && !sigHeader.isBlank()) {
                event = Webhook.constructEvent(payload, sigHeader, stripeWebhookSecret);
            } else {
                // If webhook secret is not configured, parse event without verification (dev mode).
                event = Event.GSON.fromJson(payload, Event.class);
            }

            if ("payment_intent.succeeded".equals(event.getType())) {
                PaymentIntent pi = (PaymentIntent) event.getDataObjectDeserializer()
                        .getObject()
                        .orElse(null);
                if (pi != null) {
                    String formationIdStr = pi.getMetadata() != null ? pi.getMetadata().get("formationId") : null;
                    if (formationIdStr != null) {
                        Long formationId = Long.parseLong(formationIdStr);
                        formationRepository.findById(formationId).ifPresent(f -> handlePaidFormation(formationId, f, pi.getId()));
                    }
                }
            }

            return ResponseEntity.ok(Map.of("received", true));
        } catch (SignatureVerificationException e) {
            return ResponseEntity.status(400).body(Map.of("error", "Invalid signature"));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }

    private void handlePaidFormation(Long formationId, LlcFormation f, String paymentIntentId) {
        if (f.getNorthwestCheckoutCompletedAt() != null) {
            return;
        }
        markPaid(f, paymentIntentId);
        formationRepository.findById(formationId).ifPresent(fresh ->
                llcFormationNorthwestShoppingCartService.submitAfterPaymentIfNeeded(fresh));
    }

    private void markPaid(LlcFormation f, String paymentIntentId) {
        f.setStatus("PAID");
        f.setStripePaymentIntentId(paymentIntentId);
        f.setPaidAt(OffsetDateTime.now());
        formationRepository.save(f);
    }
}

