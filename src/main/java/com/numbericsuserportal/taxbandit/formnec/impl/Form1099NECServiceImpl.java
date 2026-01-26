package com.numbericsuserportal.taxbandit.formnec.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.numbericsuserportal.taxbandit.auth.TaxBanditsAuthService;
import com.numbericsuserportal.taxbandit.formnec.dto.CreateForm1099NECRequestDTO;
import com.numbericsuserportal.taxbandit.formnec.dto.CreateForm1099NECResponseDTO;
import com.numbericsuserportal.taxbandit.formnec.service.Form1099NECService;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class Form1099NECServiceImpl implements Form1099NECService {

    @Autowired
    private TaxBanditsAuthService taxBanditsAuthService;

    @Value("${taxbandits.api.base.url:https://testapi.taxbandits.com/V1.7.3}")
    private String apiBaseUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Configure RestTemplate with HttpComponentsClientHttpRequestFactory for proper header handling
    public Form1099NECServiceImpl() {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        factory.setConnectTimeout(java.time.Duration.ofSeconds(30));
        factory.setConnectionRequestTimeout(java.time.Duration.ofSeconds(30));
        this.restTemplate = new RestTemplate(factory);
    }

    @Override
    public CreateForm1099NECResponseDTO createForm1099NEC(CreateForm1099NECRequestDTO request) {
        try {
            // Get access token
            String accessToken = taxBanditsAuthService.getAccessToken();
            
            // Validate access token
            if (accessToken == null || accessToken.isEmpty()) {
                throw new RuntimeException("Access token is null or empty. OAuth authentication may have failed.");
            }

            // Prepare request
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            String requestBody = objectMapper.writeValueAsString(request);
            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

            // API endpoint - Handle trailing slash in base URL
            String baseUrl = apiBaseUrl.endsWith("/") ? apiBaseUrl : apiBaseUrl + "/";
            String apiUrl = baseUrl + "Form1099NEC/Create";

            System.out.println("=== TaxBandits Create Form 1099-NEC ===");
            System.out.println("API URL: " + apiUrl);
            System.out.println("Access Token: " + (accessToken != null ? accessToken.substring(0, Math.min(50, accessToken.length())) + "..." : "NULL"));
            System.out.println("Request Body: " + requestBody);
            System.out.println("=======================================");

            // Make API call
            ResponseEntity<CreateForm1099NECResponseDTO> response = restTemplate.exchange(
                apiUrl,
                HttpMethod.POST,
                entity,
                CreateForm1099NECResponseDTO.class
            );

            CreateForm1099NECResponseDTO responseBody = response.getBody();
            
            System.out.println("=== TaxBandits Create Form 1099-NEC Response ===");
            System.out.println("Status Code: " + (responseBody != null ? responseBody.getStatusCode() : "null"));
            System.out.println("Status Name: " + (responseBody != null ? responseBody.getStatusName() : "null"));
            System.out.println("Submission ID: " + (responseBody != null ? responseBody.getSubmissionId() : "null"));
            System.out.println("================================================");

            return responseBody;

        } catch (Exception e) {
            System.err.println("Error creating Form 1099-NEC: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to create Form 1099-NEC: " + e.getMessage(), e);
        }
    }
}

