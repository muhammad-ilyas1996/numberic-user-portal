package com.numbericsuserportal.stripeintegration.service;

import com.numbericsuserportal.stripeintegration.config.NMIConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * NMI Payment Service for Tax Payments
 * Implements NMI Direct Post API for transaction processing
 * Documentation: https://secure.nmi.com/resellers/resources/integration/
 */
@Service
public class NMIPaymentService {

    @Autowired
    private NMIConfig nmiConfig;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Process a direct payment through NMI Direct Post API
     * @param amount Amount in dollars
     * @param cardNumber Credit card number
     * @param cardExpiry Card expiry (MMYY format)
     * @param cardCvv CVV code
     * @param description Payment description
     * @param customerInfo Customer information
     * @return Payment response
     * @throws Exception if payment fails
     */
    public NMIPaymentResponse processPayment(
            Double amount,
            String cardNumber,
            String cardExpiry,
            String cardCvv,
            String description,
            CustomerInfo customerInfo) throws Exception {

        // Validate inputs
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }
        if (cardNumber == null || cardNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Card number is required");
        }
        if (cardExpiry == null || cardExpiry.trim().isEmpty()) {
            throw new IllegalArgumentException("Card expiry is required (MMYY format)");
        }
        if (cardCvv == null || cardCvv.trim().isEmpty()) {
            throw new IllegalArgumentException("CVV is required");
        }

        if (!nmiConfig.isConfigured()) {
            throw new IllegalStateException("NMI is not configured. Please check application.properties");
        }

        try {
            // NMI Direct Post API uses form-encoded data
            MultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>();
            
            // Authentication - Use API key if available, otherwise username/password
            if (nmiConfig.getNmiApiKey() != null && !nmiConfig.getNmiApiKey().isEmpty()) {
                requestParams.add("security_key", nmiConfig.getNmiApiKey());
            } else {
                requestParams.add("username", nmiConfig.getNmiUsername());
                requestParams.add("password", nmiConfig.getNmiPassword());
            }
            
            // Transaction type: sale
            requestParams.add("type", "sale");
            
            // Amount (NMI expects dollars with 2 decimal places)
            requestParams.add("amount", String.format("%.2f", amount));
            
            // Card information
            requestParams.add("ccnumber", cardNumber.replaceAll("\\s", "")); // Remove spaces
            requestParams.add("ccexp", cardExpiry); // MMYY format
            requestParams.add("cvv", cardCvv);
            
            // Description
            if (description != null && !description.trim().isEmpty()) {
                requestParams.add("description", description);
            }
            
            // Customer information (optional but recommended for tax payments)
            if (customerInfo != null) {
                if (customerInfo.getFirstName() != null) {
                    requestParams.add("firstname", customerInfo.getFirstName());
                }
                if (customerInfo.getLastName() != null) {
                    requestParams.add("lastname", customerInfo.getLastName());
                }
                if (customerInfo.getEmail() != null) {
                    requestParams.add("email", customerInfo.getEmail());
                }
                if (customerInfo.getAddress() != null) {
                    requestParams.add("address1", customerInfo.getAddress());
                }
                if (customerInfo.getCity() != null) {
                    requestParams.add("city", customerInfo.getCity());
                }
                if (customerInfo.getState() != null) {
                    requestParams.add("state", customerInfo.getState());
                }
                if (customerInfo.getZip() != null) {
                    requestParams.add("zip", customerInfo.getZip());
                }
                if (customerInfo.getCountry() != null) {
                    requestParams.add("country", customerInfo.getCountry());
                }
                if (customerInfo.getPhone() != null) {
                    requestParams.add("phone", customerInfo.getPhone());
                }
            }
            
            // Set headers for form-encoded data
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(requestParams, headers);
            
            // Send request to NMI Transaction API
            ResponseEntity<String> response = restTemplate.postForEntity(
                nmiConfig.getNmiTransactionUrl(),
                request,
                String.class
            );
            
            // Parse NMI response
            return parseNMIResponse(response.getBody());
            
        } catch (Exception e) {
            throw new Exception("NMI API request failed: " + e.getMessage(), e);
        }
    }

    /**
     * Process payment using vault ID (stored payment method)
     * @param amount Amount in dollars
     * @param vaultId Vault ID from previous transaction
     * @param description Payment description
     * @return Payment response
     * @throws Exception if payment fails
     */
    public NMIPaymentResponse processPaymentWithVault(
            Double amount,
            String vaultId,
            String description) throws Exception {

        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }
        if (vaultId == null || vaultId.trim().isEmpty()) {
            throw new IllegalArgumentException("Vault ID is required");
        }

        if (!nmiConfig.isConfigured()) {
            throw new IllegalStateException("NMI is not configured. Please check application.properties");
        }

        try {
            MultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>();
            
            // Authentication
            if (nmiConfig.getNmiApiKey() != null && !nmiConfig.getNmiApiKey().isEmpty()) {
                requestParams.add("security_key", nmiConfig.getNmiApiKey());
            } else {
                requestParams.add("username", nmiConfig.getNmiUsername());
                requestParams.add("password", nmiConfig.getNmiPassword());
            }
            
            // Transaction type: sale
            requestParams.add("type", "sale");
            
            // Amount
            requestParams.add("amount", String.format("%.2f", amount));
            
            // Use vault_id for stored payment method
            requestParams.add("vault_id", vaultId);
            
            // Description
            if (description != null && !description.trim().isEmpty()) {
                requestParams.add("description", description);
            }
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(requestParams, headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(
                nmiConfig.getNmiTransactionUrl(),
                request,
                String.class
            );
            
            return parseNMIResponse(response.getBody());
            
        } catch (Exception e) {
            throw new Exception("NMI API request failed: " + e.getMessage(), e);
        }
    }

    /**
     * Parse NMI API response
     * NMI returns response in format: response=1&responsetext=Transaction%20Approved&transactionid=123456
     */
    private NMIPaymentResponse parseNMIResponse(String response) {
        if (response == null || response.isEmpty()) {
            return new NMIPaymentResponse(
                false,
                null,
                "Empty response from NMI",
                null,
                null
            );
        }

        Map<String, String> responseMap = parseQueryString(response);
        
        String responseCode = responseMap.get("response");
        String responseText = responseMap.get("responsetext");
        String transactionId = responseMap.get("transactionid");
        String vaultId = responseMap.get("vault_id"); // May be returned for new vaults
        String authCode = responseMap.get("authcode"); // Authorization code
        
        // NMI response codes: 1 = Approved, 2 = Declined, 3 = Error
        boolean success = "1".equals(responseCode);
        
        return new NMIPaymentResponse(
            success,
            transactionId,
            responseText != null ? responseText : "No response message",
            vaultId,
            authCode
        );
    }

    /**
     * Parse query string format response
     */
    private Map<String, String> parseQueryString(String queryString) {
        Map<String, String> params = new HashMap<>();
        if (queryString == null || queryString.isEmpty()) {
            return params;
        }
        
        String[] pairs = queryString.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=", 2);
            if (keyValue.length == 2) {
                String key = keyValue[0];
                try {
                    String value = java.net.URLDecoder.decode(keyValue[1], java.nio.charset.StandardCharsets.UTF_8);
                    params.put(key, value);
                } catch (Exception e) {
                    params.put(key, keyValue[1]);
                }
            } else if (keyValue.length == 1) {
                params.put(keyValue[0], "");
            }
        }
        return params;
    }

    /**
     * Payment Response DTO
     */
    public static class NMIPaymentResponse {
        private boolean success;
        private String transactionId;
        private String message;
        private String vaultId; // For storing payment method for future use
        private String authCode; // Authorization code

        public NMIPaymentResponse(boolean success, String transactionId, String message, String vaultId, String authCode) {
            this.success = success;
            this.transactionId = transactionId;
            this.message = message;
            this.vaultId = vaultId;
            this.authCode = authCode;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getTransactionId() {
            return transactionId;
        }

        public String getMessage() {
            return message;
        }

        public String getVaultId() {
            return vaultId;
        }

        public String getAuthCode() {
            return authCode;
        }
    }

    /**
     * Customer Information DTO
     */
    public static class CustomerInfo {
        private String firstName;
        private String lastName;
        private String email;
        private String address;
        private String city;
        private String state;
        private String zip;
        private String country;
        private String phone;

        // Getters and Setters
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }

        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }

        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }

        public String getState() { return state; }
        public void setState(String state) { this.state = state; }

        public String getZip() { return zip; }
        public void setZip(String zip) { this.zip = zip; }

        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
    }
}

