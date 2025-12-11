package com.numbericsuserportal.taxkintsugi.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.numbericsuserportal.taxkintsugi.converter.TransactionConverter;
import com.numbericsuserportal.taxkintsugi.dto.TransactionDTO;
import com.numbericsuserportal.taxkintsugi.entity.TransactionEntity;
import com.numbericsuserportal.taxkintsugi.repo.TransactionRepository;
import com.numbericsuserportal.taxkintsugi.service.TaxService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import javax.net.ssl.*;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class TaxServiceImpl implements TaxService {

    @Value("${kintsugi.api.base-url}")
    private String baseUrl;

    @Value("${kintsugi.api.key}")
    private String apiKey;

    @Value("${kintsugi.api.organization-id}")
    private String organizationId;

    private final TransactionRepository transactionRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper snakeCaseObjectMapper;

    public TaxServiceImpl(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
        this.snakeCaseObjectMapper = createSnakeCaseObjectMapper();
        this.restTemplate = createRestTemplateWithSSL();
    }

    /**
     * Create ObjectMapper with snake_case naming strategy and Java 8 time support for Kintsugi API
     */
    private ObjectMapper createSnakeCaseObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

        mapper.registerModule(new JavaTimeModule());

        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper;
    }

    /**
     * Create RestTemplate with SSL support and snake_case JSON serialization for Kintsugi API
     */
    private RestTemplate createRestTemplateWithSSL() {
        try {
            // Trust all certificates (for development/testing)
            // For production, use proper certificate validation
            TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() { 
                        return null; 
                    }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) { }
                    public void checkServerTrusted(X509Certificate[] certs, String authType) { }
                }
            };

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);

            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(30000); // 30 seconds
            factory.setReadTimeout(30000); // 30 seconds

            RestTemplate restTemplate = new RestTemplate(factory);
            
            // Configure RestTemplate to use snake_case ObjectMapper for Kintsugi API
            List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
            MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
            jsonConverter.setObjectMapper(snakeCaseObjectMapper);
            messageConverters.add(jsonConverter);
            restTemplate.setMessageConverters(messageConverters);

            return restTemplate;
        } catch (Exception e) {
            System.err.println("Warning: Failed to configure SSL for RestTemplate: " + e.getMessage());
            // Fallback to default RestTemplate with snake_case mapper
            RestTemplate restTemplate = new RestTemplate();
            List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
            MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
            jsonConverter.setObjectMapper(snakeCaseObjectMapper);
            messageConverters.add(jsonConverter);
            restTemplate.setMessageConverters(messageConverters);
            return restTemplate;
        }
    }

    @Override
    public TransactionDTO calculateTax(TransactionDTO requestDto) {
        try {
            // Trim baseUrl to remove any whitespace (especially from inline comments)
            String trimmedBaseUrl = baseUrl != null ? baseUrl.trim() : "";
            
            // Remove any inline comments if present (e.g., "https://api.trykintsugi.com  # comment")
            if (trimmedBaseUrl.contains("#")) {
                trimmedBaseUrl = trimmedBaseUrl.substring(0, trimmedBaseUrl.indexOf("#")).trim();
            }
            
            // Validate URL format
            if (trimmedBaseUrl.isEmpty()) {
                throw new RuntimeException("Kintsugi API base URL is not configured");
            }
            
            // Validate URL format
            try {
                new URL(trimmedBaseUrl);
            } catch (Exception e) {
                throw new RuntimeException("Invalid Kintsugi API base URL format: " + trimmedBaseUrl, e);
            }

            // Build full URL (ensure no double slashes)
            String endpoint = "/v1/tax/estimate";
            String fullUrl = trimmedBaseUrl.endsWith("/") 
                ? trimmedBaseUrl + endpoint.substring(1)  // Remove leading slash from endpoint
                : trimmedBaseUrl + endpoint;

            // Validate API credentials
            if (apiKey == null || apiKey.trim().isEmpty()) {
                throw new RuntimeException("Kintsugi API key is not configured");
            }
            if (organizationId == null || organizationId.trim().isEmpty()) {
                throw new RuntimeException("Kintsugi organization ID is not configured");
            }

            // Set Kintsugi headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-api-key", apiKey.trim());
            headers.set("x-organization-id", organizationId.trim());

            HttpEntity<TransactionDTO> httpEntity = new HttpEntity<>(requestDto, headers);

            // Debug logging
            System.out.println("=== Kintsugi API Call Debug ===");
            System.out.println("Base URL (trimmed): " + trimmedBaseUrl);
            System.out.println("Full URL: " + fullUrl);
            System.out.println("API Key (first 10 chars): " + (apiKey != null && apiKey.length() > 10 ? apiKey.substring(0, 10) + "..." : "NOT SET"));
            System.out.println("Organization ID: " + organizationId);
            System.out.println("Request Body: " + requestDto);
            System.out.println("==============================");

            // Call Kintsugi API
            ResponseEntity<Map> response = restTemplate.exchange(
                    fullUrl,
                    HttpMethod.POST,
                    httpEntity,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                // Store dynamic tax response
                requestDto.setTaxResponse(response.getBody());

                // Persist transaction with addresses/items cascade
                TransactionEntity entity = TransactionConverter.toEntity(requestDto);
                transactionRepository.save(entity);

                return requestDto;
            } else {
                throw new RuntimeException("Failed to calculate tax: " + response.getStatusCode());
            }

        } catch (RestClientException e) {
            // More detailed error message for RestClientException
            String errorMsg = "Error calling Kintsugi API: " + e.getMessage();
            if (e.getCause() != null) {
                errorMsg += " | Cause: " + e.getCause().getMessage();
            }
            System.err.println("Kintsugi API RestClientException: " + errorMsg);
            e.printStackTrace();
            throw new RuntimeException(errorMsg, e);
        } catch (Exception e) {
            String errorMsg = "Error calling Kintsugi API: " + e.getMessage();
            System.err.println("Kintsugi API Exception: " + errorMsg);
            e.printStackTrace();
            throw new RuntimeException(errorMsg, e);
        }
    }
}