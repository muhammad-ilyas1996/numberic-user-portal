package com.numbericsuserportal.kintsugi.client;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class KintsugiApiClient {

    @Value("${kintsugi.api.base-url:https://api.trykintsugi.com}")
    private String baseUrl;

    @Value("${kintsugi.api.key:}")
    private String apiKey;

    @Value("${kintsugi.api.organization-id:}")
    private String organizationId;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public KintsugiApiClient() {
        this.objectMapper = createObjectMapper();
        this.restTemplate = createRestTemplate();
    }

    public <T> T get(String path, Class<T> responseType, Map<String, String> queryParams) {
        return exchange(path, HttpMethod.GET, null, responseType, null, queryParams);
    }

    public <T> T get(String path, ParameterizedTypeReference<T> typeRef, Map<String, String> queryParams) {
        return exchange(path, HttpMethod.GET, null, null, typeRef, queryParams);
    }

    public <T> T post(String path, Object body, Class<T> responseType) {
        return exchange(path, HttpMethod.POST, body, responseType, null, Map.of());
    }

    public <T> T post(String path, Object body, ParameterizedTypeReference<T> typeRef) {
        return exchange(path, HttpMethod.POST, body, null, typeRef, Map.of());
    }

    private <T> T exchange(
            String path,
            HttpMethod method,
            Object body,
            Class<T> responseType,
            ParameterizedTypeReference<T> typeRef,
            Map<String, String> queryParams) {
        validateConfig();

        String trimmedBase = normalizeBaseUrl(baseUrl);
        String url = trimmedBase + (path.startsWith("/") ? path : "/" + path);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
        if (queryParams != null) {
            queryParams.forEach(builder::queryParam);
        }

        HttpHeaders headers = buildHeaders();
        HttpEntity<Object> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<T> response;
            if (typeRef != null) {
                response = restTemplate.exchange(builder.toUriString(), method, entity, typeRef);
            } else {
                response = restTemplate.exchange(builder.toUriString(), method, entity, responseType);
            }
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new IllegalStateException("Kintsugi API failed: " + response.getStatusCode());
            }
            return response.getBody();
        } catch (RestClientException e) {
            throw new IllegalStateException("Kintsugi API error on " + path + ": " + e.getMessage(), e);
        }
    }

    private void validateConfig() {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Kintsugi API key is not configured (kintsugi.api.key)");
        }
        if (organizationId == null || organizationId.isBlank()) {
            throw new IllegalStateException("Kintsugi organization ID is not configured (kintsugi.api.organization-id)");
        }
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", apiKey.trim());
        headers.set("x-organization-id", organizationId.trim());
        return headers;
    }

    private String normalizeBaseUrl(String raw) {
        String trimmed = raw != null ? raw.trim() : "";
        if (trimmed.contains("#")) {
            trimmed = trimmed.substring(0, trimmed.indexOf("#")).trim();
        }
        if (trimmed.isEmpty()) {
            throw new IllegalStateException("Kintsugi API base URL is not configured");
        }
        try {
            new URL(trimmed);
        } catch (Exception e) {
            throw new IllegalStateException("Invalid Kintsugi API base URL: " + trimmed, e);
        }
        return trimmed.endsWith("/") ? trimmed.substring(0, trimmed.length() - 1) : trimmed;
    }

    private ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper;
    }

    private RestTemplate createRestTemplate() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }

                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }

                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }
                    }
            };
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
        } catch (Exception ignored) {
            // fallback to default SSL
        }

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(30000);
        factory.setReadTimeout(30000);

        RestTemplate template = new RestTemplate(factory);
        List<org.springframework.http.converter.HttpMessageConverter<?>> converters = new ArrayList<>();
        MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
        jsonConverter.setObjectMapper(objectMapper);
        converters.add(jsonConverter);
        template.setMessageConverters(converters);
        return template;
    }
}
