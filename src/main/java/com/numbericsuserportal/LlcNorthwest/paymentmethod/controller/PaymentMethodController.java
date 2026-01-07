package com.numbericsuserportal.LlcNorthwest.paymentmethod.controller;

import com.numbericsuserportal.LlcNorthwest.paymentmethod.dto.CreatePaymentMethodRequestDTO;
import com.numbericsuserportal.LlcNorthwest.paymentmethod.dto.PaymentMethodActionResponseDTO;
import com.numbericsuserportal.LlcNorthwest.paymentmethod.dto.PaymentMethodsResponseDTO;
import com.numbericsuserportal.LlcNorthwest.paymentmethod.dto.UpdatePaymentMethodRequestDTO;
import com.numbericsuserportal.LlcNorthwest.paymentmethod.service.PaymentMethodService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * REST Controller for Payment Method operations
 */
@RestController
@RequestMapping("/api/llc-northwest/payment-methods")
@CrossOrigin(origins = "*")
public class PaymentMethodController {

    @Autowired
    private PaymentMethodService paymentMethodService;

    /**
     * GET /api/llc-northwest/payment-methods
     * Returns a list of all saved card payment methods for the authorized account
     */
    @GetMapping
    public ResponseEntity<?> getPaymentMethods() {
        try {
            PaymentMethodsResponseDTO response = paymentMethodService.getPaymentMethods();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * POST /api/llc-northwest/payment-methods
     * Creates a new saved card payment method
     * Only valid, active cards are accepted. Test cards are not supported.
     */
    @PostMapping
    public ResponseEntity<?> createPaymentMethod(@Valid @RequestBody CreatePaymentMethodRequestDTO request) {
        try {
            PaymentMethodActionResponseDTO response = paymentMethodService.createPaymentMethod(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * PATCH /api/llc-northwest/payment-methods/{id}
     * Updates an existing saved card payment method
     */
    @PatchMapping("/{id}")
    public ResponseEntity<?> updatePaymentMethod(
            @PathVariable UUID id,
            @RequestBody UpdatePaymentMethodRequestDTO request) {
        try {
            PaymentMethodActionResponseDTO response = paymentMethodService.updatePaymentMethod(id, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * DELETE /api/llc-northwest/payment-methods/{id}
     * Deletes a saved card payment method
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePaymentMethod(@PathVariable UUID id) {
        try {
            PaymentMethodActionResponseDTO response = paymentMethodService.deletePaymentMethod(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }
}

