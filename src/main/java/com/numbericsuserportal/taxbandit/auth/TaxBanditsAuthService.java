package com.numbericsuserportal.taxbandit.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class TaxBanditsAuthService {

    private static final Logger log = LoggerFactory.getLogger(TaxBanditsAuthService.class);

    @Value("${taxbandits.client.id}")
    private String clientId;

    @Value("${taxbandits.client.secret}")
    private String clientSecret;

    @Value("${taxbandits.user.token}")
    private String userToken;

    @Value("${taxbandits.oauth.base.url:https://testoauth.expressauth.net/v2}")
    private String baseUrl;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate;

    // Configure RestTemplate with HttpComponentsClientHttpRequestFactory for proper header handling
    public TaxBanditsAuthService() {
        // RestTemplate is still used for getServerTime() method
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        factory.setConnectTimeout(java.time.Duration.ofSeconds(30));
        factory.setConnectionRequestTimeout(java.time.Duration.ofSeconds(30));
        this.restTemplate = new RestTemplate(factory);
    }
    
    // Cache for access token (optional - can be enhanced with expiration check)
    private String cachedAccessToken;
    private Long tokenExpiresAt;

    /**
     * Generate JWS (JSON Web Signature) for TaxBandits OAuth 2.0
     * Following TaxBandits documentation format
     */
    public String generateJWS() {
        try {
            // Get current Unix timestamp (seconds since epoch)
            // Subtract 45 seconds to account for server time sync (as per Postman script)
            long currentTime = (System.currentTimeMillis() / 1000) - 45;

            // JWT Header - Use LinkedHashMap to maintain order
            Map<String, Object> header = new LinkedHashMap<>();
            header.put("alg", "HS256");
            header.put("typ", "JWT");

            // JWT Payload - Use LinkedHashMap to maintain order
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("iss", clientId);  // Issuer: Client ID
            payload.put("sub", clientId);  // Subject: Client ID
            payload.put("aud", userToken); // Audience: User Token
            payload.put("iat", currentTime); // Issued at: Unix timestamp (adjusted by -45 seconds)

            // Base64URL encode header and payload
            String encodedHeader = base64UrlEncode(objectMapper.writeValueAsString(header));
            String encodedPayload = base64UrlEncode(objectMapper.writeValueAsString(payload));

            // Create signature using HMAC SHA256
            String signatureInput = encodedHeader + "." + encodedPayload;
            byte[] signatureBytes = hmacSHA256(signatureInput, clientSecret);
            String signature = base64UrlEncodeBytes(signatureBytes);

            // Build JWS token
            return encodedHeader + "." + encodedPayload + "." + signature;

        } catch (Exception e) {
            log.error("Failed to generate TaxBandits JWS: {}", e.getMessage());
            throw new RuntimeException("Failed to generate TaxBandits JWS: " + e.getMessage(), e);
        }
    }

    /**
     * Get Access Token from TaxBandits OAuth server
     * Returns the access token (JWT) to be used in subsequent API calls
     */
    public String getAccessToken() {
        try {
            // Check if cached token is still valid
            if (cachedAccessToken != null && tokenExpiresAt != null) {
                long currentTime = System.currentTimeMillis() / 1000;
                // If token expires in more than 5 minutes, use cached token
                if (tokenExpiresAt > currentTime + 300) {
                    log.debug("Using cached TaxBandits access token");
                    return cachedAccessToken;
                }
            }

            // Validate credentials are loaded
            if (clientId == null || clientId.isEmpty()) {
                throw new RuntimeException("TaxBandits Client ID is not configured");
            }
            if (clientSecret == null || clientSecret.isEmpty()) {
                throw new RuntimeException("TaxBandits Client Secret is not configured");
            }
            if (userToken == null || userToken.isEmpty()) {
                throw new RuntimeException("TaxBandits User Token is not configured");
            }

            // Generate JWS
            String jws = generateJWS();
            
            // Validate JWS is generated
            if (jws == null || jws.isEmpty()) {
                throw new RuntimeException("Failed to generate JWS token");
            }

            // Handle trailing slash in base URL (official SDK format: https://testoauth.expressauth.net/v2/)
            String oauthBaseUrl = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
            String authUrl = oauthBaseUrl + "tbsauth";
            // According to TaxBandits sample Java code, header name should be "Authentication" (not "Authorization")
            // JWS token should be sent directly without "Bearer " prefix
            String authHeaderValue = jws;
            
            // Use Apache HttpClient 5 for reliable header handling
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpGet httpGet = new HttpGet(authUrl);
            
            // CRITICAL: TaxBandits API expects "Authentication" header, NOT "Authorization"
            // As per TaxBandits sample Java code documentation
            httpGet.setHeader("Authentication", authHeaderValue);
            httpGet.setHeader("Content-Type", "application/json");
            httpGet.setHeader("Accept", "application/json");
            
            TaxBanditsTokenResponse tokenResponse;
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                int statusCode = response.getCode();
                String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                log.debug("TaxBandits auth response: {} - {} chars", statusCode, responseBody.length());
                
                if (statusCode == 200) {
                    tokenResponse = objectMapper.readValue(responseBody, TaxBanditsTokenResponse.class);
                } else {
                    // Try to parse error response
                    try {
                        tokenResponse = objectMapper.readValue(responseBody, TaxBanditsTokenResponse.class);
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to get access token. Status: " + statusCode + 
                            ", Body: " + responseBody);
                    }
                }
            } finally {
                httpClient.close();
            }

            if (tokenResponse != null && tokenResponse.getStatusCode() == 200 && tokenResponse.getAccessToken() != null) {
                // Cache the token
                cachedAccessToken = tokenResponse.getAccessToken();
                // Calculate expiration time (current time + expiresIn seconds)
                long currentTime = System.currentTimeMillis() / 1000;
                tokenExpiresAt = currentTime + tokenResponse.getExpiresIn();
                log.debug("TaxBandits access token obtained, expires in {}s", tokenResponse.getExpiresIn());
                return tokenResponse.getAccessToken();
            } else {
                String errorMsg = "Failed to get access token. Status: " + 
                    (tokenResponse != null ? tokenResponse.getStatusCode() : "null");
                if (tokenResponse != null && tokenResponse.getErrors() != null) {
                    errorMsg += ", Errors: " + tokenResponse.getErrors();
                }
                throw new RuntimeException(errorMsg);
            }


        } catch (Exception e) {
            log.error("Failed to get TaxBandits access token: {}", e.getMessage());
            throw new RuntimeException("Failed to get TaxBandits access token: " + e.getMessage(), e);
        }
    }

    /**
     * Get server time from TaxBandits API
     * Used to sync system time with TaxBandits server time
     */
    public TaxBanditsServerTimeResponse getServerTime() {
        try {
            String serverTimeUrl = baseUrl + "/getservertime";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<TaxBanditsServerTimeResponse> response = restTemplate.exchange(
                serverTimeUrl,
                HttpMethod.GET,
                entity,
                TaxBanditsServerTimeResponse.class
            );

            return response.getBody();

        } catch (Exception e) {
            log.error("Failed to get TaxBandits server time: {}", e.getMessage());
            throw new RuntimeException("Failed to get TaxBandits server time: " + e.getMessage(), e);
        }
    }

    /**
     * Base64URL encode for String input
     */
    private String base64UrlEncode(String input) {
        byte[] encoded = Base64.getEncoder().encode(input.getBytes(StandardCharsets.UTF_8));
        String encodedString = new String(encoded, StandardCharsets.UTF_8);
        return encodedString
                .replace("=", "")  // Remove padding
                .replace("+", "-")  // Replace + with -
                .replace("/", "_");  // Replace / with _
    }

    /**
     * Base64URL encode for byte[] input
     */
    private String base64UrlEncodeBytes(byte[] input) {
        byte[] encoded = Base64.getEncoder().encode(input);
        String encodedString = new String(encoded, StandardCharsets.UTF_8);
        return encodedString
                .replace("=", "")  // Remove padding
                .replace("+", "-")  // Replace + with -
                .replace("/", "_");  // Replace / with _
    }

    /**
     * HMAC SHA256 signature generation
     */
    private byte[] hmacSHA256(String data, String key) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKeySpec);
        return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Clear cached access token (useful for testing or forced refresh)
     */
    public void clearCachedToken() {
        cachedAccessToken = null;
        tokenExpiresAt = null;
    }
}

