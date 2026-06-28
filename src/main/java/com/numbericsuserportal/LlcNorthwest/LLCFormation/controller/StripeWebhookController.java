package com.numbericsuserportal.LlcNorthwest.LLCFormation.controller;

import com.numbericsuserportal.LlcNorthwest.LLCFormation.entity.LlcFormation;
import com.numbericsuserportal.LlcNorthwest.LLCFormation.repo.LlcFormationRepository;
import com.numbericsuserportal.LlcNorthwest.LLCFormation.service.LlcFormationNorthwestIntegrationService;
import com.numbericsuserportal.LlcNorthwest.LLCFormation.service.LlcFormationNorthwestShoppingCartService;
import com.numbericsuserportal.usermanagement.domain.User;
import com.numbericsuserportal.usermanagement.repo.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.net.Webhook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/webhooks")
@CrossOrigin(origins = "*")
public class StripeWebhookController {

    private static final Logger log = LoggerFactory.getLogger(StripeWebhookController.class);

    @Autowired
    private LlcFormationRepository formationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LlcFormationNorthwestIntegrationService northwestIntegrationService;

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
            boolean verified = stripeWebhookSecret != null && !stripeWebhookSecret.isBlank()
                    && sigHeader != null && !sigHeader.isBlank();
            if (verified) {
                event = Webhook.constructEvent(payload, sigHeader, stripeWebhookSecret);
            } else {
                log.warn("Stripe webhook received without signature verification (configure stripe.webhook.secret)");
                event = Event.GSON.fromJson(payload, Event.class);
            }

            log.info("Stripe webhook received: type={}, id={}, verified={}", event.getType(), event.getId(), verified);

            if ("payment_intent.succeeded".equals(event.getType())) {
                Optional<PaymentIntent> piOpt = resolvePaymentIntent(event);
                if (piOpt.isEmpty()) {
                    log.error("payment_intent.succeeded: could not deserialize PaymentIntent from event {}", event.getId());
                    return ResponseEntity.ok(Map.of("received", true, "warning", "payment_intent not deserialized"));
                }
                PaymentIntent pi = piOpt.get();
                String formationIdStr = pi.getMetadata() != null ? pi.getMetadata().get("formationId") : null;
                if (formationIdStr == null || formationIdStr.isBlank()) {
                    log.warn("payment_intent.succeeded for {} has no formationId metadata — skipping LLC formation handler",
                            pi.getId());
                    return ResponseEntity.ok(Map.of("received", true));
                }
                Long formationId = Long.parseLong(formationIdStr.trim());
                log.info("Processing LLC formation payment: formationId={}, paymentIntentId={}", formationId, pi.getId());
                formationRepository.findById(formationId).ifPresentOrElse(
                        f -> handlePaidFormation(formationId, f, pi.getId()),
                        () -> log.warn("Formation {} not found for payment_intent {}", formationId, pi.getId()));
            }

            return ResponseEntity.ok(Map.of("received", true));
        } catch (SignatureVerificationException e) {
            log.error("Stripe webhook signature verification failed", e);
            return ResponseEntity.status(400).body(Map.of("error", "Invalid signature"));
        } catch (Exception e) {
            log.error("Stripe webhook processing failed", e);
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Stripe API version mismatches can make getObject() empty; deserializeUnsafe is the documented fallback.
     */
    private Optional<PaymentIntent> resolvePaymentIntent(Event event) {
        if (event.getDataObjectDeserializer().getObject().isPresent()) {
            StripeObject obj = event.getDataObjectDeserializer().getObject().get();
            if (obj instanceof PaymentIntent pi) {
                return Optional.of(pi);
            }
        }
        try {
            StripeObject unsafe = event.getDataObjectDeserializer().deserializeUnsafe();
            if (unsafe instanceof PaymentIntent pi) {
                log.info("PaymentIntent resolved via deserializeUnsafe for event {}", event.getId());
                return Optional.of(pi);
            }
        } catch (Exception e) {
            log.warn("deserializeUnsafe failed for event {}: {}", event.getId(), e.getMessage());
        }
        return Optional.empty();
    }

    private void handlePaidFormation(Long formationId, LlcFormation f, String paymentIntentId) {
        if (f.getNorthwestCheckoutCompletedAt() != null) {
            log.info("Formation {} already submitted to NW — skipping duplicate webhook", formationId);
            return;
        }
        markPaid(f, paymentIntentId);
        log.info("Formation {} marked PAID via webhook (paymentIntent={})", formationId, paymentIntentId);
        formationRepository.findById(formationId).ifPresent(fresh -> {
            User user = userRepository.findById(fresh.getUserId()).orElse(null);
            if (user != null) {
                try {
                    northwestIntegrationService.ensureReadyForSubmit(fresh, user);
                } catch (Exception e) {
                    log.error("NW prepare failed for formation {} on webhook", formationId, e);
                    return;
                }
            } else {
                log.warn("User not found for formation {} — skipping NW prepare", formationId);
            }
            formationRepository.findById(formationId).ifPresent(updated -> {
                llcFormationNorthwestShoppingCartService.submitAfterPaymentIfNeeded(updated);
                log.info("Formation {} post-webhook status={}, filingStatus={}",
                        formationId, updated.getStatus(), updated.getFilingStatus());
            });
        });
    }

    private void markPaid(LlcFormation f, String paymentIntentId) {
        f.setStatus("PAID");
        f.setStripePaymentIntentId(paymentIntentId);
        f.setPaidAt(OffsetDateTime.now());
        formationRepository.save(f);
    }
}

