package com.numbericsuserportal.kintsugi.service;

import com.numbericsuserportal.kintsugi.client.KintsugiApiClient;
import com.numbericsuserportal.kintsugi.dto.ProductCategoryDTO;
import com.numbericsuserportal.kintsugi.dto.ProductSubCategoryDTO;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class KintsugiProductService {

    private final KintsugiApiClient apiClient;

    public KintsugiProductService(KintsugiApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public List<ProductCategoryDTO> getCategories() {
        List<ProductCategoryDTO> categories = apiClient.get(
                "/v1/products/categories",
                new ParameterizedTypeReference<List<ProductCategoryDTO>>() {},
                null);
        if (categories == null) {
            return List.of();
        }
        return categories;
    }

    public List<ProductSubCategoryDTO> getSubcategories(String categoryName) {
        if (categoryName == null || categoryName.isBlank()) {
            throw new IllegalArgumentException("category name is required");
        }
        String normalized = categoryName.trim();
        return getCategories().stream()
                .filter(c -> c.getName() != null
                        && c.getName().trim().equalsIgnoreCase(normalized))
                .findFirst()
                .map(ProductCategoryDTO::getSubcategories)
                .orElseThrow(() -> new IllegalArgumentException("Unknown product category: " + categoryName));
    }

    public ProductCategoryDTO getCategory(String categoryName) {
        if (categoryName == null || categoryName.isBlank()) {
            throw new IllegalArgumentException("category name is required");
        }
        String normalized = categoryName.trim();
        return getCategories().stream()
                .filter(c -> c.getName() != null
                        && c.getName().trim().equalsIgnoreCase(normalized))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown product category: " + categoryName));
    }
}
