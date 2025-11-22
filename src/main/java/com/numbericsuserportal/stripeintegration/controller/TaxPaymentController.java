package com.numbericsuserportal.stripeintegration.controller;

import com.numbericsuserportal.stripeintegration.service.NMIPaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

/**
 * Controller for Tax Payments using NMI
 * Handles tax payments, invoice payments, and other tax-related transactions
 * Based on NMI Direct Post API
 */
@RestController
@RequestMapping("/api/tax-payment")
@CrossOrigin(origins = "*")
public class TaxPaymentController {

    @Autowired
    private NMIPaymentService nmiPaymentService;

    /**
     * Process tax payment with card details
     * POST /api/tax-payment/process
     */
    @PostMapping("/process")
    public ResponseEntity<?> processTaxPayment(@RequestBody Map<String, Object> request) {
        try {
            // Extract payment details
            Double amount = getDoubleValue(request, "amount");
            String cardNumber = (String) request.get("cardNumber");
            String cardExpiry = (String) request.get("cardExpiry"); // MMYY format
            String cardCvv = (String) request.get("cardCvv");
            String description = (String) request.get("description");
            
            // Validate required fields
            if (amount == null || amount <= 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Valid amount is required"));
            }
            if (cardNumber == null || cardNumber.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Card number is required"));
            }
            if (cardExpiry == null || cardExpiry.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Card expiry is required (MMYY format, e.g., 1225)"));
            }
            if (cardCvv == null || cardCvv.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "CVV is required"));
            }

            // Extract customer info (optional)
            NMIPaymentService.CustomerInfo customerInfo = null;
            if (request.containsKey("customerInfo")) {
                @SuppressWarnings("unchecked")
                Map<String, String> customerMap = (Map<String, String>) request.get("customerInfo");
                customerInfo = new NMIPaymentService.CustomerInfo();
                customerInfo.setFirstName(customerMap.get("firstName"));
                customerInfo.setLastName(customerMap.get("lastName"));
                customerInfo.setEmail(customerMap.get("email"));
                customerInfo.setAddress(customerMap.get("address"));
                customerInfo.setCity(customerMap.get("city"));
                customerInfo.setState(customerMap.get("state"));
                customerInfo.setZip(customerMap.get("zip"));
                customerInfo.setCountry(customerMap.get("country"));
                customerInfo.setPhone(customerMap.get("phone"));
            }

            // Process payment
            NMIPaymentService.NMIPaymentResponse response = nmiPaymentService.processPayment(
                    amount,
                    cardNumber,
                    cardExpiry,
                    cardCvv,
                    description != null ? description : "Tax Payment",
                    customerInfo
            );

            Map<String, Object> result = new HashMap<>();
            result.put("success", response.isSuccess());
            result.put("transactionId", response.getTransactionId());
            result.put("message", response.getMessage());
            result.put("vaultId", response.getVaultId()); // For future payments
            result.put("authCode", response.getAuthCode()); // Authorization code

            if (response.isSuccess()) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body(result);
            }

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Payment processing failed: " + e.getMessage()));
        }
    }

    /**
     * Process tax payment using stored vault ID
     * POST /api/tax-payment/process-with-vault
     */
    @PostMapping("/process-with-vault")
    public ResponseEntity<?> processTaxPaymentWithVault(@RequestBody Map<String, Object> request) {
        try {
            Double amount = getDoubleValue(request, "amount");
            String vaultId = (String) request.get("vaultId");
            String description = (String) request.get("description");

            if (amount == null || amount <= 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Valid amount is required"));
            }
            if (vaultId == null || vaultId.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Vault ID is required"));
            }

            NMIPaymentService.NMIPaymentResponse response = nmiPaymentService.processPaymentWithVault(
                    amount,
                    vaultId,
                    description != null ? description : "Tax Payment"
            );

            Map<String, Object> result = new HashMap<>();
            result.put("success", response.isSuccess());
            result.put("transactionId", response.getTransactionId());
            result.put("message", response.getMessage());
            result.put("authCode", response.getAuthCode());

            if (response.isSuccess()) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body(result);
            }

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Payment processing failed: " + e.getMessage()));
        }
    }

    /**
     * Process payment for invoice/tax
     * POST /api/tax-payment/pay-invoice
     */
    @PostMapping("/pay-invoice")
    public ResponseEntity<?> payInvoice(@RequestBody Map<String, Object> request) {
        try {
            Long invoiceId = getLongValue(request, "invoiceId");
            Double amount = getDoubleValue(request, "amount");
            String cardNumber = (String) request.get("cardNumber");
            String cardExpiry = (String) request.get("cardExpiry");
            String cardCvv = (String) request.get("cardCvv");

            if (invoiceId == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Invoice ID is required"));
            }
            if (amount == null || amount <= 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Valid amount is required"));
            }
            if (cardNumber == null || cardNumber.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Card number is required"));
            }
            if (cardExpiry == null || cardExpiry.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Card expiry is required (MMYY format)"));
            }
            if (cardCvv == null || cardCvv.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "CVV is required"));
            }

            // Extract customer info from request
            NMIPaymentService.CustomerInfo customerInfo = null;
            if (request.containsKey("customerInfo")) {
                @SuppressWarnings("unchecked")
                Map<String, String> customerMap = (Map<String, String>) request.get("customerInfo");
                customerInfo = new NMIPaymentService.CustomerInfo();
                customerInfo.setFirstName(customerMap.get("firstName"));
                customerInfo.setLastName(customerMap.get("lastName"));
                customerInfo.setEmail(customerMap.get("email"));
                customerInfo.setAddress(customerMap.get("address"));
                customerInfo.setCity(customerMap.get("city"));
                customerInfo.setState(customerMap.get("state"));
                customerInfo.setZip(customerMap.get("zip"));
                customerInfo.setCountry(customerMap.get("country"));
                customerInfo.setPhone(customerMap.get("phone"));
            }

            String description = "Invoice Payment - Invoice #" + invoiceId;
            if (request.get("description") != null) {
                description = (String) request.get("description");
            }

            NMIPaymentService.NMIPaymentResponse response = nmiPaymentService.processPayment(
                    amount,
                    cardNumber,
                    cardExpiry,
                    cardCvv,
                    description,
                    customerInfo
            );

            Map<String, Object> result = new HashMap<>();
            result.put("success", response.isSuccess());
            result.put("transactionId", response.getTransactionId());
            result.put("message", response.getMessage());
            result.put("vaultId", response.getVaultId());
            result.put("authCode", response.getAuthCode());
            result.put("invoiceId", invoiceId);

            if (response.isSuccess()) {
                // TODO: Update invoice status in database
                // invoiceService.updateInvoicePaymentStatus(invoiceId, response.getTransactionId());
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body(result);
            }

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Payment processing failed: " + e.getMessage()));
        }
    }

    // Helper methods
    private Double getDoubleValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof Double) return (Double) value;
        if (value instanceof Number) return ((Number) value).doubleValue();
        if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private Long getLongValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof Long) return (Long) value;
        if (value instanceof Number) return ((Number) value).longValue();
        if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}

