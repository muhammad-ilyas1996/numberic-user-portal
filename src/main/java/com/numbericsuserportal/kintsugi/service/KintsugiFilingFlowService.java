package com.numbericsuserportal.kintsugi.service;

import com.numbericsuserportal.kintsugi.catalog.UsStateCatalog;
import com.numbericsuserportal.kintsugi.client.KintsugiApiClient;
import com.numbericsuserportal.kintsugi.dto.FilingFlowEstimateRequestDTO;
import com.numbericsuserportal.kintsugi.dto.FilingFlowStateDetailDTO;
import com.numbericsuserportal.kintsugi.dto.ProductCategoryDTO;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class KintsugiFilingFlowService {

    private final KintsugiProductService productService;
    private final KintsugiNexusService nexusService;
    private final KintsugiApiClient apiClient;

    public KintsugiFilingFlowService(
            KintsugiProductService productService,
            KintsugiNexusService nexusService,
            KintsugiApiClient apiClient) {
        this.productService = productService;
        this.nexusService = nexusService;
        this.apiClient = apiClient;
    }

    public List<ProductCategoryDTO> step1Categories() {
        return productService.getCategories();
    }

    public List<?> step2Subcategories(String categoryName) {
        return productService.getSubcategories(categoryName);
    }

    public List<?> step3States() {
        return nexusService.listStatesWithNexus();
    }

    public FilingFlowStateDetailDTO step3StateDetail(
            String stateCode,
            String category,
            String subcategory,
            Double sampleAmount) {
        String code = stateCode.trim().toUpperCase();
        Map<String, Object> nexus = nexusService.getStateNexusDetail(code);

        Map<String, Object> taxEstimate = null;
        if (category != null && !category.isBlank()
                && subcategory != null && !subcategory.isBlank()) {
            FilingFlowEstimateRequestDTO estimateRequest = new FilingFlowEstimateRequestDTO();
            estimateRequest.setCategory(category);
            estimateRequest.setSubcategory(subcategory);
            estimateRequest.setStateCode(code);
            estimateRequest.setAmount(sampleAmount != null ? sampleAmount : 100.0);
            estimateRequest.setQuantity(1.0);
            estimateRequest.setSimulateActiveRegistration(true);
            taxEstimate = estimateTax(estimateRequest);
        }

        return new FilingFlowStateDetailDTO(
                code,
                UsStateCatalog.resolveName(code).orElse(code),
                nexus,
                taxEstimate
        );
    }

    public Map<String, Object> estimateTax(FilingFlowEstimateRequestDTO request) {
        validateEstimateRequest(request);

        String stateCode = request.getStateCode().trim().toUpperCase();
        double amount = request.getAmount() != null ? request.getAmount() : 0.0;
        double quantity = request.getQuantity() != null ? request.getQuantity() : 1.0;

        Map<String, Object> body = new HashMap<>();
        body.put("date", Instant.now().toString());
        body.put("external_id", "numbrics-estimate-" + UUID.randomUUID());
        body.put("currency", "USD");
        body.put("source", "OTHER");
        body.put("marketplace", false);

        if (Boolean.TRUE.equals(request.getSimulateActiveRegistration())) {
            body.put("simulate_active_registration", true);
        }

        Map<String, Object> address = new HashMap<>();
        address.put("type", "SHIP_TO");
        address.put("state", stateCode);
        address.put("country", "US");
        if (request.getCity() != null && !request.getCity().isBlank()) {
            address.put("city", request.getCity());
        }
        if (request.getPostalCode() != null && !request.getPostalCode().isBlank()) {
            address.put("postal_code", request.getPostalCode());
        }
        body.put("addresses", List.of(address));

        Map<String, Object> item = new HashMap<>();
        item.put("external_id", "item-" + UUID.randomUUID());
        item.put("date", Instant.now().toString());
        item.put("external_product_id", "prod-" + UUID.randomUUID());
        item.put("product_category", normalizeCategory(request.getCategory()));
        item.put("product_subcategory", request.getSubcategory().trim());
        item.put("quantity", quantity);
        item.put("amount", amount);
        body.put("transaction_items", List.of(item));

        return apiClient.post(
                "/v1/tax/estimate",
                body,
                new ParameterizedTypeReference<Map<String, Object>>() {});
    }

    private void validateEstimateRequest(FilingFlowEstimateRequestDTO request) {
        if (request.getStateCode() == null || request.getStateCode().isBlank()) {
            throw new IllegalArgumentException("stateCode is required");
        }
        if (!UsStateCatalog.isValid(request.getStateCode())) {
            throw new IllegalArgumentException("Invalid stateCode: " + request.getStateCode());
        }
        if (request.getCategory() == null || request.getCategory().isBlank()) {
            throw new IllegalArgumentException("category is required");
        }
        if (request.getSubcategory() == null || request.getSubcategory().isBlank()) {
            throw new IllegalArgumentException("subcategory is required");
        }
    }

    /**
     * Kintsugi expects enum-style category values (e.g. PHYSICAL) while the categories API
     * returns display names (e.g. Physical). Normalize common values.
     */
    private String normalizeCategory(String category) {
        String trimmed = category.trim();
        if (trimmed.equalsIgnoreCase("physical")) {
            return "PHYSICAL";
        }
        if (trimmed.equalsIgnoreCase("digital")) {
            return "DIGITAL";
        }
        if (trimmed.equalsIgnoreCase("service")) {
            return "SERVICE";
        }
        if (trimmed.equalsIgnoreCase("miscellaneous")) {
            return "MISCELLANEOUS";
        }
        return trimmed.toUpperCase().replace(' ', '_');
    }
}
