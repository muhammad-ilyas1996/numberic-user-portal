package com.numbericsuserportal.LlcNorthwest.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class CorporateToolsAuthService {

    @Value("${corporate.tools.access.key}")
    private String accessKey;

    @Value("${corporate.tools.secret.key}")
    private String secretKey;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Generate JWT token for Corporate Tools API
     * Manual implementation matching Postman script exactly
     */
    public String generateToken(String path, String queryString, String requestBody) {
        try {
            // Calculate SHA-256 hash of queryString + requestBody
            String contentInput = queryString + requestBody;
            String content = generateSHA256Hash(contentInput);

            // Debug logging
            System.out.println("=== JWT Token Generation ===");
            System.out.println("Path: " + path);
            System.out.println("Query String: " + queryString);
            System.out.println("Request Body: " + requestBody);
            System.out.println("Content Input (queryString + requestBody): " + contentInput);
            System.out.println("Content Hash (SHA256): " + content);
            System.out.println("Access Key (first 20 chars): " + (accessKey != null ? accessKey.substring(0, Math.min(20, accessKey.length())) + "..." : "NULL"));
            System.out.println("Secret Key loaded: " + (secretKey != null && !secretKey.isEmpty() ? "YES (length: " + secretKey.length() + ")" : "NO"));

            // JWT Header (exact format as Postman script)
            Map<String, Object> header = new HashMap<>();
            header.put("access_key", accessKey);
            header.put("alg", "HS256");
            header.put("typ", "JWT");

            // JWT Payload
            Map<String, Object> payload = new HashMap<>();
            payload.put("path", path);
            payload.put("content", content);

            System.out.println("JWT Header: " + header);
            System.out.println("JWT Payload: " + payload);

            // Manual Base64URL encoding (exact as Postman script)
            String encodedHeader = base64UrlEncode(objectMapper.writeValueAsString(header));
            String encodedPayload = base64UrlEncode(objectMapper.writeValueAsString(payload));

            // Create signature
            String signatureInput = encodedHeader + "." + encodedPayload;
            byte[] signatureBytes = hmacSHA256(signatureInput, secretKey);
            String signature = base64UrlEncodeBytes(signatureBytes);

            // Build token
            String token = encodedHeader + "." + encodedPayload + "." + signature;

            System.out.println("Encoded Header: " + encodedHeader);
            System.out.println("Encoded Payload: " + encodedPayload);
            System.out.println("Signature: " + signature);
            System.out.println("Generated Token: " + token);
            System.out.println("============================");

            return token;

        } catch (Exception e) {
            System.err.println("Token Generation Error: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to generate Corporate Tools JWT token: " + e.getMessage(), e);
        }
    }

    /**
     * Base64URL encode (exact as Postman script) - for String input
     */
    private String base64UrlEncode(String input) {
        byte[] encoded = Base64.getEncoder().encode(input.getBytes(StandardCharsets.UTF_8));
        String encodedString = new String(encoded, StandardCharsets.UTF_8);
        // Replace characters as per Base64URL spec
        return encodedString
                .replace("=", "")  // Remove padding
                .replace("+", "-")  // Replace + with -
                .replace("/", "_");  // Replace / with _
    }

    /**
     * Base64URL encode (exact as Postman script) - for byte[] input
     */
    private String base64UrlEncodeBytes(byte[] input) {
        byte[] encoded = Base64.getEncoder().encode(input);
        String encodedString = new String(encoded, StandardCharsets.UTF_8);
        // Replace characters as per Base64URL spec
        return encodedString
                .replace("=", "")  // Remove padding
                .replace("+", "-")  // Replace + with -
                .replace("/", "_");  // Replace / with _
    }

    /**
     * HMAC SHA256 (exact as Postman script)
     */
    private byte[] hmacSHA256(String data, String key) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKeySpec);
        return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generate SHA256 hash
     */
    private String generateSHA256Hash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate SHA256 hash: " + e.getMessage(), e);
        }
    }

    /**
     * Generate token for GET request
     */
    public String generateTokenForGet(String path, String queryString) {
        return generateToken(path, queryString != null ? queryString : "", "");
    }

    /**
     * Generate token for POST/PUT request
     */
    public String generateTokenForPost(String path, String requestBody) {
        return generateToken(path, "", requestBody != null ? requestBody : "");
    }
}

