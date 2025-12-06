package com.numbericsuserportal.LlcNorthwest.impl;

import com.numbericsuserportal.LlcNorthwest.converter.FilingProductConverter;
import com.numbericsuserportal.LlcNorthwest.dto.FilingProductDTO;
import com.numbericsuserportal.LlcNorthwest.dto.FilingProductsResponseDTO;
import com.numbericsuserportal.LlcNorthwest.entity.FilingProductEntity;
import com.numbericsuserportal.LlcNorthwest.repo.FilingProductRepository;
import com.numbericsuserportal.LlcNorthwest.service.CorporateToolsApiService;
import com.numbericsuserportal.LlcNorthwest.service.FilingProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FilingProductServiceImpl implements FilingProductService {

    @Autowired
    private CorporateToolsApiService corporateToolsApiService;

    @Autowired
    private FilingProductRepository filingProductRepository;

    @Autowired
    private FilingProductConverter filingProductConverter;

    @Override
    @Transactional
    public FilingProductsResponseDTO fetchAndSaveFilingProducts(
            String websiteUrl,
            String jurisdiction,
            String entityType) {
        
        // Fetch from API
        FilingProductsResponseDTO apiResponse = corporateToolsApiService.getFilingProducts(
            websiteUrl,
            jurisdiction,
            entityType
        );

        if (apiResponse.getSuccess() && apiResponse.getResult() != null) {
            // Save to database
            for (FilingProductDTO productDTO : apiResponse.getResult()) {
                saveFilingProduct(productDTO);
            }
        }

        return apiResponse;
    }

    @Override
    public FilingProductsResponseDTO getFilingProductsFromDatabase(String websiteUrl) {
        // Get from database
        List<FilingProductEntity> entities = filingProductRepository.findAll();
        
        FilingProductsResponseDTO response = new FilingProductsResponseDTO();
        response.setSuccess(true);
        response.setTimestamp(java.time.LocalDateTime.now().toString());
        response.setResult(
            entities.stream()
                .map(filingProductConverter::toDTO)
                .collect(Collectors.toList())
        );
        
        return response;
    }

    private void saveFilingProduct(FilingProductDTO productDTO) {
        // Check if already exists
        FilingProductEntity existing = filingProductRepository
            .findByExternalId(productDTO.getId())
            .orElse(null);

        if (existing == null) {
            // Create new
            FilingProductEntity entity = filingProductConverter.toEntity(productDTO);
            filingProductRepository.save(entity);
        } else {
            // Update existing
            filingProductConverter.updateEntity(existing, productDTO);
            filingProductRepository.save(existing);
        }
    }
}

