package com.numbericsuserportal.LlcNorthwest.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.numbericsuserportal.LlcNorthwest.auth.CorporateToolsAuthService;
import com.numbericsuserportal.LlcNorthwest.companies.dto.CompaniesResponseDTO;
import com.numbericsuserportal.LlcNorthwest.companies.dto.CompanyDTO;
import com.numbericsuserportal.LlcNorthwest.companies.dto.CreateCompanyRequestDTO;
import com.numbericsuserportal.LlcNorthwest.companies.dto.UpdateCompanyRequestDTO;
import com.numbericsuserportal.LlcNorthwest.dto.FilingProductsResponseDTO;
import com.numbericsuserportal.LlcNorthwest.service.CorporateToolsApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.UUID;

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

    @Override
    public CompaniesResponseDTO getCompanies(Integer limit, Integer offset, String[] names) {
        try {
            String path = "/companies";
            
            // Build query string manually
            StringBuilder queryBuilder = new StringBuilder();
            if (limit != null) {
                queryBuilder.append("limit=").append(limit);
            }
            if (offset != null) {
                if (queryBuilder.length() > 0) queryBuilder.append("&");
                queryBuilder.append("offset=").append(offset);
            }
            if (names != null && names.length > 0) {
                if (queryBuilder.length() > 0) queryBuilder.append("&");
                queryBuilder.append("names=").append(String.join(",", names));
            }
            
            String queryString = queryBuilder.toString();
            String token = authService.generateTokenForGet(path, queryString);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            String url = baseUrl + path + (queryString.isEmpty() ? "" : "?" + queryString);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                String.class
            );
            
            return objectMapper.readValue(response.getBody(), CompaniesResponseDTO.class);
            
        } catch (Exception e) {
            System.err.println("Error getting companies: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to get companies: " + e.getMessage(), e);
        }
    }

    @Override
    public CompaniesResponseDTO getCompanyById(UUID companyId) {
        try {
            String path = "/companies/" + companyId.toString();
            String token = authService.generateTokenForGet(path, "");
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            String url = baseUrl + path;
            
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                String.class
            );
            
            // Single company response - API returns result as single object, not array
            String responseBody = response.getBody();
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            
            CompaniesResponseDTO fullResponse = new CompaniesResponseDTO();
            fullResponse.setSuccess(jsonNode.get("success").asBoolean());
            fullResponse.setTimestamp(jsonNode.has("timestamp") ? jsonNode.get("timestamp").asText() : null);
            
            // Parse result as single CompanyDTO object
            JsonNode resultNode = jsonNode.get("result");
            if (resultNode != null && resultNode.isObject()) {
                CompanyDTO company = objectMapper.treeToValue(resultNode, CompanyDTO.class);
                fullResponse.setResult(List.of(company));
            }
            
            return fullResponse;
            
        } catch (Exception e) {
            System.err.println("Error getting company by ID: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to get company by ID: " + e.getMessage(), e);
        }
    }

    @Override
    public CompaniesResponseDTO createCompanies(CreateCompanyRequestDTO request) {
        try {
            String path = "/companies";
            String requestBody = objectMapper.writeValueAsString(request);
            String token = authService.generateTokenForPost(path, requestBody);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
            String url = baseUrl + path;
            
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                String.class
            );
            
            return objectMapper.readValue(response.getBody(), CompaniesResponseDTO.class);
            
        } catch (Exception e) {
            System.err.println("Error creating companies: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to create companies: " + e.getMessage(), e);
        }
    }

    @Override
    public CompaniesResponseDTO updateCompanies(UpdateCompanyRequestDTO request) {
        try {
            String path = "/companies";
            String requestBody = objectMapper.writeValueAsString(request);
            String token = authService.generateTokenForPost(path, requestBody);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
            String url = baseUrl + path;
            
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.PATCH,
                entity,
                String.class
            );
            
            return objectMapper.readValue(response.getBody(), CompaniesResponseDTO.class);
            
        } catch (Exception e) {
            System.err.println("Error updating companies: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to update companies: " + e.getMessage(), e);
        }
    }
}

