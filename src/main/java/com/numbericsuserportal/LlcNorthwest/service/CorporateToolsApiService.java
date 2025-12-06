package com.numbericsuserportal.LlcNorthwest.service;

import com.numbericsuserportal.LlcNorthwest.dto.FilingProductsResponseDTO;

public interface CorporateToolsApiService {
    FilingProductsResponseDTO getFilingProducts(String websiteUrl, String jurisdiction, String entityType);
    FilingProductsResponseDTO getFilingProductsOfferings(String companyId, String jurisdiction);
}

