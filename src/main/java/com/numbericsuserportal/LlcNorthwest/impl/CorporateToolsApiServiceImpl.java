package com.numbericsuserportal.LlcNorthwest.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.numbericsuserportal.LlcNorthwest.auth.CorporateToolsAuthService;
import com.numbericsuserportal.LlcNorthwest.companies.dto.CompaniesResponseDTO;
import com.numbericsuserportal.LlcNorthwest.companies.dto.CompanyDTO;
import com.numbericsuserportal.LlcNorthwest.companies.dto.CreateCompanyRequestDTO;
import com.numbericsuserportal.LlcNorthwest.companies.dto.UpdateCompanyRequestDTO;
import com.numbericsuserportal.LlcNorthwest.dto.FilingProductsResponseDTO;
import com.numbericsuserportal.LlcNorthwest.complianceevents.dto.ComplianceEventsResponseDTO;
import com.numbericsuserportal.LlcNorthwest.filingmethod.dto.FilingMethodSchemaResponseDTO;
import com.numbericsuserportal.LlcNorthwest.filingmethod.dto.FilingMethodsResponseDTO;
import com.numbericsuserportal.LlcNorthwest.document.dto.*;
import com.numbericsuserportal.LlcNorthwest.paymentmethod.dto.CreatePaymentMethodRequestDTO;
import com.numbericsuserportal.LlcNorthwest.paymentmethod.dto.PaymentMethodActionResponseDTO;
import com.numbericsuserportal.LlcNorthwest.paymentmethod.dto.PaymentMethodsResponseDTO;
import com.numbericsuserportal.LlcNorthwest.paymentmethod.dto.UpdatePaymentMethodRequestDTO;
import com.numbericsuserportal.LlcNorthwest.registeredagent.dto.RegisteredAgentProductsResponseDTO;
import com.numbericsuserportal.LlcNorthwest.signedforms.dto.SignedFormsResponseDTO;
import com.numbericsuserportal.LlcNorthwest.service.CorporateToolsApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;

import java.util.List;
import java.util.UUID;

@Service
public class CorporateToolsApiServiceImpl implements CorporateToolsApiService {

    @Autowired
    private CorporateToolsAuthService authService;

    @Value("${corporate.tools.api.base.url}")
    private String baseUrl;

    // Configure RestTemplate with HttpComponentsClientHttpRequestFactory to support PATCH method
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CorporateToolsApiServiceImpl() {
        // Use Apache HttpClient which properly supports PATCH method
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        factory.setConnectTimeout(java.time.Duration.ofSeconds(30)); // 30 seconds
        factory.setConnectionRequestTimeout(java.time.Duration.ofSeconds(30)); // 30 seconds
        this.restTemplate = new RestTemplate(factory);
    }

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

    @Override
    public FilingMethodsResponseDTO getFilingMethods(UUID companyId, UUID filingProductId, String jurisdiction) {
        try {
            String path = "/filing-methods";
            
            // Build query string manually
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("company_id=").append(companyId.toString());
            queryBuilder.append("&filing_product_id=").append(filingProductId.toString());
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
            
            return objectMapper.readValue(response.getBody(), FilingMethodsResponseDTO.class);
            
        } catch (Exception e) {
            System.err.println("Error getting filing methods: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to get filing methods: " + e.getMessage(), e);
        }
    }

    @Override
    public FilingMethodSchemaResponseDTO getFilingMethodSchemas(UUID companyId, UUID filingMethodId) {
        try {
            String path = "/filing-methods/schemas";
            
            // Build query string manually
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("company_id=").append(companyId.toString());
            queryBuilder.append("&filing_method_id=").append(filingMethodId.toString());
            
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
            
            return objectMapper.readValue(response.getBody(), FilingMethodSchemaResponseDTO.class);
            
        } catch (Exception e) {
            System.err.println("Error getting filing method schemas: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to get filing method schemas: " + e.getMessage(), e);
        }
    }

    @Override
    public ComplianceEventsResponseDTO getComplianceEvents(
            Integer limit,
            Integer offset,
            String company,
            UUID companyId,
            String startDate,
            String endDate,
            String[] jurisdictions,
            UUID[] jurisdictionIds) {
        try {
            String path = "/compliance-events";
            
            // Build query string manually
            StringBuilder queryBuilder = new StringBuilder();
            
            // Required parameters
            queryBuilder.append("start_date=").append(startDate);
            queryBuilder.append("&end_date=").append(endDate);
            
            // Optional parameters
            if (limit != null) {
                queryBuilder.append("&limit=").append(limit);
            }
            if (offset != null) {
                queryBuilder.append("&offset=").append(offset);
            }
            
            // Either company OR company_id (not both)
            if (company != null && !company.trim().isEmpty()) {
                queryBuilder.append("&company=").append(company);
            } else if (companyId != null) {
                queryBuilder.append("&company_id=").append(companyId.toString());
            }
            
            // Either jurisdictions OR jurisdiction_ids (not both)
            if (jurisdictions != null && jurisdictions.length > 0) {
                queryBuilder.append("&jurisdictions=").append(String.join(",", jurisdictions));
            } else if (jurisdictionIds != null && jurisdictionIds.length > 0) {
                String[] jurisdictionIdStrings = new String[jurisdictionIds.length];
                for (int i = 0; i < jurisdictionIds.length; i++) {
                    jurisdictionIdStrings[i] = jurisdictionIds[i].toString();
                }
                queryBuilder.append("&jurisdiction_ids=").append(String.join(",", jurisdictionIdStrings));
            }
            
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
            
            return objectMapper.readValue(response.getBody(), ComplianceEventsResponseDTO.class);
            
        } catch (Exception e) {
            System.err.println("Error getting compliance events: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to get compliance events: " + e.getMessage(), e);
        }
    }

    @Override
    public RegisteredAgentProductsResponseDTO getRegisteredAgentProducts(String url) {
        try {
            String path = "/registered-agent-products";
            
            // Build query string manually
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("url=").append(url);
            
            String queryString = queryBuilder.toString();
            String token = authService.generateTokenForGet(path, queryString);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            String fullUrl = baseUrl + path + "?" + queryString;
            
            ResponseEntity<String> response = restTemplate.exchange(
                fullUrl,
                HttpMethod.GET,
                entity,
                String.class
            );
            
            return objectMapper.readValue(response.getBody(), RegisteredAgentProductsResponseDTO.class);
            
        } catch (Exception e) {
            System.err.println("Error getting registered agent products: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to get registered agent products: " + e.getMessage(), e);
        }
    }

    @Override
    public SignedFormsResponseDTO getSignedForms(UUID filingMethodId, UUID websiteId) {
        try {
            String path = "/signed-forms";
            
            // Build query string manually
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("filing_method_id=").append(filingMethodId.toString());
            queryBuilder.append("&website_id=").append(websiteId.toString());
            
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
            
            return objectMapper.readValue(response.getBody(), SignedFormsResponseDTO.class);
            
        } catch (Exception e) {
            System.err.println("Error getting signed forms: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to get signed forms: " + e.getMessage(), e);
        }
    }

    // ==================== PAYMENT METHODS API ====================

    @Override
    public PaymentMethodsResponseDTO getPaymentMethods() {
        try {
            String path = "/payment-methods";
            String token = authService.generateTokenForGet(path, "");
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            String url = baseUrl + path;
            
            System.out.println("=== Get Payment Methods ===");
            System.out.println("URL: " + url);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                String.class
            );
            
            System.out.println("Response Status: " + response.getStatusCode());
            System.out.println("Response Body: " + response.getBody());
            
            return objectMapper.readValue(response.getBody(), PaymentMethodsResponseDTO.class);
            
        } catch (Exception e) {
            System.err.println("Error getting payment methods: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to get payment methods: " + e.getMessage(), e);
        }
    }

    @Override
    public PaymentMethodActionResponseDTO createPaymentMethod(CreatePaymentMethodRequestDTO request) {
        try {
            String path = "/payment-methods";
            String requestBody = objectMapper.writeValueAsString(request);
            String token = authService.generateTokenForPost(path, requestBody);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
            String url = baseUrl + path;
            
            System.out.println("=== Create Payment Method ===");
            System.out.println("URL: " + url);
            System.out.println("Request Body: " + requestBody);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                String.class
            );
            
            System.out.println("Response Status: " + response.getStatusCode());
            System.out.println("Response Body: " + response.getBody());
            
            return objectMapper.readValue(response.getBody(), PaymentMethodActionResponseDTO.class);
            
        } catch (Exception e) {
            System.err.println("Error creating payment method: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to create payment method: " + e.getMessage(), e);
        }
    }

    @Override
    public PaymentMethodActionResponseDTO updatePaymentMethod(UUID id, UpdatePaymentMethodRequestDTO request) {
        try {
            String path = "/payment-methods/" + id.toString();
            String requestBody = objectMapper.writeValueAsString(request);
            String token = authService.generateTokenForPost(path, requestBody);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
            String url = baseUrl + path;
            
            System.out.println("=== Update Payment Method ===");
            System.out.println("URL: " + url);
            System.out.println("Request Body: " + requestBody);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.PATCH,
                entity,
                String.class
            );
            
            System.out.println("Response Status: " + response.getStatusCode());
            System.out.println("Response Body: " + response.getBody());
            
            return objectMapper.readValue(response.getBody(), PaymentMethodActionResponseDTO.class);
            
        } catch (Exception e) {
            System.err.println("Error updating payment method: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to update payment method: " + e.getMessage(), e);
        }
    }

    @Override
    public PaymentMethodActionResponseDTO deletePaymentMethod(UUID id) {
        try {
            String path = "/payment-methods/" + id.toString();
            String token = authService.generateTokenForGet(path, "");
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            String url = baseUrl + path;
            
            System.out.println("=== Delete Payment Method ===");
            System.out.println("URL: " + url);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.DELETE,
                entity,
                String.class
            );
            
            System.out.println("Response Status: " + response.getStatusCode());
            System.out.println("Response Body: " + response.getBody());
            
            return objectMapper.readValue(response.getBody(), PaymentMethodActionResponseDTO.class);
            
        } catch (Exception e) {
            System.err.println("Error deleting payment method: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to delete payment method: " + e.getMessage(), e);
        }
    }

    // ==================== DOCUMENTS API ====================

    @Override
    public DocumentsResponseDTO getDocuments(
            Integer limit,
            Integer offset,
            String status,
            String start,
            String stop,
            String jurisdiction,
            UUID companyId) {
        try {
            String path = "/documents";
            
            // Build query string
            StringBuilder queryBuilder = new StringBuilder();
            if (limit != null) {
                queryBuilder.append("limit=").append(limit);
            }
            if (offset != null) {
                if (queryBuilder.length() > 0) queryBuilder.append("&");
                queryBuilder.append("offset=").append(offset);
            }
            if (status != null && !status.isEmpty()) {
                if (queryBuilder.length() > 0) queryBuilder.append("&");
                queryBuilder.append("status=").append(status);
            }
            if (start != null && !start.isEmpty()) {
                if (queryBuilder.length() > 0) queryBuilder.append("&");
                queryBuilder.append("start=").append(start);
            }
            if (stop != null && !stop.isEmpty()) {
                if (queryBuilder.length() > 0) queryBuilder.append("&");
                queryBuilder.append("stop=").append(stop);
            }
            if (jurisdiction != null && !jurisdiction.isEmpty()) {
                if (queryBuilder.length() > 0) queryBuilder.append("&");
                queryBuilder.append("jurisdiction=").append(jurisdiction);
            }
            if (companyId != null) {
                if (queryBuilder.length() > 0) queryBuilder.append("&");
                queryBuilder.append("company_id=").append(companyId.toString());
            }
            
            String queryString = queryBuilder.toString();
            String token = authService.generateTokenForGet(path, queryString);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            String url = baseUrl + path + (queryString.isEmpty() ? "" : "?" + queryString);
            
            System.out.println("=== Get Documents ===");
            System.out.println("URL: " + url);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                String.class
            );
            
            System.out.println("Response Status: " + response.getStatusCode());
            
            return objectMapper.readValue(response.getBody(), DocumentsResponseDTO.class);
            
        } catch (Exception e) {
            System.err.println("Error getting documents: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to get documents: " + e.getMessage(), e);
        }
    }

    @Override
    public DocumentResponseDTO getDocumentById(UUID id) {
        try {
            String path = "/documents/" + id.toString();
            String token = authService.generateTokenForGet(path, "");
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            String url = baseUrl + path;
            
            System.out.println("=== Get Document By ID ===");
            System.out.println("URL: " + url);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                String.class
            );
            
            System.out.println("Response Status: " + response.getStatusCode());
            
            return objectMapper.readValue(response.getBody(), DocumentResponseDTO.class);
            
        } catch (Exception e) {
            System.err.println("Error getting document by ID: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to get document by ID: " + e.getMessage(), e);
        }
    }

    @Override
    public byte[] getDocumentPage(UUID id, Integer pageNumber, Integer dpi) {
        try {
            String path = "/documents/" + id.toString() + "/page/" + pageNumber;
            
            StringBuilder queryBuilder = new StringBuilder();
            if (dpi != null) {
                queryBuilder.append("dpi=").append(dpi);
            }
            
            String queryString = queryBuilder.toString();
            String token = authService.generateTokenForGet(path, queryString);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            headers.setAccept(List.of(MediaType.IMAGE_PNG));
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            String url = baseUrl + path + (queryString.isEmpty() ? "" : "?" + queryString);
            
            System.out.println("=== Get Document Page ===");
            System.out.println("URL: " + url);
            
            ResponseEntity<byte[]> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                byte[].class
            );
            
            System.out.println("Response Status: " + response.getStatusCode());
            
            return response.getBody();
            
        } catch (Exception e) {
            System.err.println("Error getting document page: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to get document page: " + e.getMessage(), e);
        }
    }

    @Override
    public byte[] downloadDocument(UUID id) {
        try {
            String path = "/documents/" + id.toString() + "/download";
            String token = authService.generateTokenForGet(path, "");
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            headers.setAccept(List.of(MediaType.APPLICATION_PDF));
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            String url = baseUrl + path;
            
            System.out.println("=== Download Document ===");
            System.out.println("URL: " + url);
            
            ResponseEntity<byte[]> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                byte[].class
            );
            
            System.out.println("Response Status: " + response.getStatusCode());
            
            return response.getBody();
            
        } catch (Exception e) {
            System.err.println("Error downloading document: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to download document: " + e.getMessage(), e);
        }
    }

    @Override
    public PageUrlResponseDTO getDocumentPageUrl(UUID id, Integer pageNumber, Integer dpi) {
        try {
            String path = "/documents/" + id.toString() + "/page/" + pageNumber + "/url";
            
            StringBuilder queryBuilder = new StringBuilder();
            if (dpi != null) {
                queryBuilder.append("dpi=").append(dpi);
            }
            
            String queryString = queryBuilder.toString();
            String token = authService.generateTokenForGet(path, queryString);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            String url = baseUrl + path + (queryString.isEmpty() ? "" : "?" + queryString);
            
            System.out.println("=== Get Document Page URL ===");
            System.out.println("URL: " + url);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                String.class
            );
            
            System.out.println("Response Status: " + response.getStatusCode());
            
            return objectMapper.readValue(response.getBody(), PageUrlResponseDTO.class);
            
        } catch (Exception e) {
            System.err.println("Error getting document page URL: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to get document page URL: " + e.getMessage(), e);
        }
    }

    @Override
    public BulkDownloadResponseDTO bulkDownloadDocuments(UUID[] ids) {
        try {
            String path = "/documents/bulk-download";
            
            // Build query string with multiple IDs
            StringBuilder queryBuilder = new StringBuilder();
            for (int i = 0; i < ids.length; i++) {
                if (i > 0) queryBuilder.append("&");
                queryBuilder.append("ids=").append(ids[i].toString());
            }
            
            String queryString = queryBuilder.toString();
            String token = authService.generateTokenForGet(path, queryString);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            String url = baseUrl + path + "?" + queryString;
            
            System.out.println("=== Bulk Download Documents ===");
            System.out.println("URL: " + url);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                String.class
            );
            
            System.out.println("Response Status: " + response.getStatusCode());
            
            return objectMapper.readValue(response.getBody(), BulkDownloadResponseDTO.class);
            
        } catch (Exception e) {
            System.err.println("Error bulk downloading documents: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to bulk download documents: " + e.getMessage(), e);
        }
    }

    @Override
    public UnlockDocumentResponseDTO unlockDocument(UUID id, UnlockDocumentRequestDTO request) {
        try {
            String path = "/documents/unlock/" + id.toString();
            String requestBody = objectMapper.writeValueAsString(request);
            String token = authService.generateTokenForPost(path, requestBody);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
            String url = baseUrl + path;
            
            System.out.println("=== Unlock Document ===");
            System.out.println("URL: " + url);
            System.out.println("Request Body: " + requestBody);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                String.class
            );
            
            System.out.println("Response Status: " + response.getStatusCode());
            System.out.println("Response Body: " + response.getBody());
            
            return objectMapper.readValue(response.getBody(), UnlockDocumentResponseDTO.class);
            
        } catch (Exception e) {
            System.err.println("Error unlocking document: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to unlock document: " + e.getMessage(), e);
        }
    }
}

