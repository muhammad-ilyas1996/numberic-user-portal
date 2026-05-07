package com.numbericsuserportal.LlcNorthwest.impl;

import com.numbericsuserportal.LlcNorthwest.converter.FilingProductConverter;
import com.numbericsuserportal.LlcNorthwest.dto.FilingMethodDTO;
import com.numbericsuserportal.LlcNorthwest.dto.FilingProductDTO;
import com.numbericsuserportal.LlcNorthwest.dto.FilingProductsResponseDTO;
import com.numbericsuserportal.LlcNorthwest.service.CorporateToolsApiService;
import com.numbericsuserportal.LlcNorthwest.service.FilingProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class FilingProductServiceImpl implements FilingProductService {

    @Autowired
    private CorporateToolsApiService corporateToolsApiService;

    @Override
    @Transactional
    public FilingProductsResponseDTO fetchAndSaveFilingProducts(
            String websiteUrl,
            String jurisdiction,
            String entityType) {
        
        // Requirement: do not persist CorporateTools/Northwest reference data.
        // Only LLCFormation journey data should be stored in our DB.
        return corporateToolsApiService.getFilingProducts(websiteUrl, jurisdiction, entityType);
    }

    @Override
    @Transactional(readOnly = true)
    public FilingProductsResponseDTO getFilingProductsFromDatabase(String websiteUrl) {
        FilingProductsResponseDTO response = new FilingProductsResponseDTO();
        response.setSuccess(true);
        response.setTimestamp(java.time.LocalDateTime.now().toString());
        response.setResult(List.of());
        return response;
    }
}

