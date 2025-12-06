package com.numbericsuserportal.LlcNorthwest.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.numbericsuserportal.LlcNorthwest.auth.CorporateToolsAuthService;
import com.numbericsuserportal.LlcNorthwest.dto.FilingProductsResponseDTO;
import com.numbericsuserportal.LlcNorthwest.service.CorporateToolsApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class CorporateToolsApiServiceImpl implements CorporateToolsApiService {

    @Autowired
    private CorporateToolsAuthService authService;

    @Value("${corporate.tools.api.base.url}")
    private String baseUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public FilingProductsResponseDTO getFilingProducts(
            String websiteUrl,
            String jurisdiction,
            String entityType) {
        
        try {
            String path = "/filing-products";
            
            // Build query string manually (without encoding) - EXACT format as Postman script
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("url=").append(websiteUrl);
            
            if (jurisdiction != null && !jurisdiction.isEmpty()) {
                queryBuilder.append("&jurisdiction=").append(jurisdiction);
            }
            
            if (entityType != null && !entityType.isEmpty()) {
                queryBuilder.append("&entity_type=").append(entityType);
            }
            
            String queryString = queryBuilder.toString();
            
            // Debug logging
            System.out.println("=== Corporate Tools API Debug ===");
            System.out.println("Path: " + path);
            System.out.println("Query String: " + queryString);
            System.out.println("Access Key loaded: " + (authService != null ? "YES" : "NO"));
            
            // Generate token
            String token = authService.generateTokenForGet(path, queryString);
            System.out.println("Generated Token: " + token);
            
            // Build URL manually (without UriComponentsBuilder encoding)
            String url = baseUrl + path + "?" + queryString;
            System.out.println("Full URL: " + url);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            System.out.println("Authorization Header: Bearer " + (token.length() > 50 ? token.substring(0, 50) + "..." : token));
            System.out.println("=================================");
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                String.class
            );
            
            System.out.println("Response Status: " + response.getStatusCode());
            System.out.println("Response Body: " + response.getBody());
            
            return objectMapper.readValue(response.getBody(), FilingProductsResponseDTO.class);
            
        } catch (Exception e) {
            System.err.println("Error Details: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to get filing products: " + e.getMessage(), e);
        }
    }

    @Override
    public FilingProductsResponseDTO getFilingProductsOfferings(
            String companyId,
            String jurisdiction) {
        
        try {
            String path = "/filing-products/offerings";
            
            // Build query string manually
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("company_id=").append(companyId);
            queryBuilder.append("&jurisdiction=").append(jurisdiction);
            
            String queryString = queryBuilder.toString();
            String token = authService.generateTokenForGet(path, queryString);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            String url = baseUrl + path + "?" + queryString;
            
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                String.class
            );
            
            return objectMapper.readValue(response.getBody(), FilingProductsResponseDTO.class);
            
        } catch (Exception e) {
            System.err.println("Error Details: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to get filing products offerings: " + e.getMessage(), e);
        }
    }
}

