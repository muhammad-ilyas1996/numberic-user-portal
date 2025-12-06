package com.numbericsuserportal.LlcNorthwest.service;

import com.numbericsuserportal.LlcNorthwest.dto.FilingProductsResponseDTO;

public interface FilingProductService {
    FilingProductsResponseDTO fetchAndSaveFilingProducts(String websiteUrl, String jurisdiction, String entityType);
    FilingProductsResponseDTO getFilingProductsFromDatabase(String websiteUrl);
}

