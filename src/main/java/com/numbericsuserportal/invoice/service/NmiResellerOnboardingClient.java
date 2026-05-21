package com.numbericsuserportal.invoice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Configurable adapter for the NMI reseller/boarding API.
 *
 * Exact reseller schemas differ by account/program, so this client forwards the prepared JSON
 * payload to configured endpoints and maps common response names back into Numbrics.
 */
@Service
public class NmiResellerOnboardingClient {

    @Value("${nmi.reseller.boarding.url:}")
    private String boardingUrl;

    @Value("${nmi.reseller.status.url-template:}")
    private String statusUrlTemplate;

    @Value("${nmi.reseller.api.key:}")
    private String apiKey;

    @Value("${nmi.reseller.username:}")
    private String username;

    @Value("${nmi.reseller.password:}")
    private String password;

    @Value("${nmi.reseller.auth.header-name:Authorization}")
    private String authHeaderName;

    @Value("${nmi.reseller.auth.header-prefix:Bearer }")
    private String authHeaderPrefix;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper;

    public NmiResellerOnboardingClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public boolean isBoardingConfigured() {
        return boardingUrl != null && !boardingUrl.isBlank();
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> submit(Map<String, Object> payload) {
        if (!isBoardingConfigured()) {
            throw new IllegalStateException("NMI reseller boarding endpoint is not configured");
        }
        ResponseEntity<Map> response = restTemplate.exchange(
            boardingUrl,
            HttpMethod.POST,
            new HttpEntity<>(payload, headers()),
            Map.class
        );
        return response.getBody() != null ? new LinkedHashMap<>(response.getBody()) : Map.of();
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> fetchStatus(String applicationId) {
        if (statusUrlTemplate == null || statusUrlTemplate.isBlank()) {
            throw new IllegalStateException("NMI reseller status endpoint is not configured");
        }
        String url = statusUrlTemplate.replace("{applicationId}", applicationId);
        ResponseEntity<Map> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            new HttpEntity<>(headers()),
            Map.class
        );
        return response.getBody() != null ? new LinkedHashMap<>(response.getBody()) : Map.of();
    }

    public String toJson(Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            return "{}";
        }
    }

    private HttpHeaders headers() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));
        if (apiKey != null && !apiKey.isBlank()) {
            headers.set(authHeaderName, (authHeaderPrefix != null ? authHeaderPrefix : "") + apiKey.trim());
        } else if (username != null && !username.isBlank() && password != null) {
            String basic = Base64.getEncoder()
                    .encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
            headers.set(HttpHeaders.AUTHORIZATION, "Basic " + basic);
        }
        return headers;
    }
}
