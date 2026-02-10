package com.numbericsuserportal.LlcNorthwest.impl;

import com.numbericsuserportal.LlcNorthwest.converter.FilingProductConverter;
import com.numbericsuserportal.LlcNorthwest.dto.FilingMethodDTO;
import com.numbericsuserportal.LlcNorthwest.dto.FilingProductDTO;
import com.numbericsuserportal.LlcNorthwest.dto.FilingProductsResponseDTO;
import com.numbericsuserportal.LlcNorthwest.entity.FilingMethodEntity;
import com.numbericsuserportal.LlcNorthwest.entity.FilingProductEntity;
import com.numbericsuserportal.LlcNorthwest.repo.FilingMethodRepository;
import com.numbericsuserportal.LlcNorthwest.repo.FilingProductRepository;
import com.numbericsuserportal.LlcNorthwest.service.CorporateToolsApiService;
import com.numbericsuserportal.LlcNorthwest.service.FilingProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FilingProductServiceImpl implements FilingProductService {

    @Autowired
    private FilingMethodRepository filingMethodRepository;
    
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
            // Collect all existing IDs from database ONCE before processing
            Set<UUID> existingProductIds = new HashSet<>();
            Set<UUID> existingMethodIds = new HashSet<>();
            
            filingProductRepository.findAll().forEach(p -> existingProductIds.add(p.getExternalId()));
            filingMethodRepository.findAll().forEach(m -> existingMethodIds.add(m.getExternalId()));
            
            System.out.println("Existing products in DB: " + existingProductIds.size());
            System.out.println("Existing methods in DB: " + existingMethodIds.size());
            
            // Track what we save in this request to avoid duplicates within same batch
            Set<UUID> savedProductIds = new HashSet<>(existingProductIds);
            Set<UUID> savedMethodIds = new HashSet<>(existingMethodIds);
            
            // Process each product
            for (FilingProductDTO productDTO : apiResponse.getResult()) {
                UUID productExternalId = productDTO.getId();
                
                // Check using our tracking set (which includes DB + already saved in this batch)
                if (savedProductIds.contains(productExternalId)) {
                    // Product already exists - UPDATE only basic fields
                    System.out.println("Updating existing product: " + productExternalId);
                    FilingProductEntity existing = filingProductRepository.findByExternalId(productExternalId).orElse(null);
                    if (existing != null) {
                        existing.setName(productDTO.getName());
                        existing.setFilingName(productDTO.getFilingName());
                        existing.setPrice(productDTO.getPrice());
                        filingProductRepository.save(existing);
                    }
                } else {
                    // Product doesn't exist - CREATE new
                    System.out.println("Creating new product: " + productExternalId);
                    FilingProductEntity newEntity = new FilingProductEntity();
                    newEntity.setExternalId(productExternalId);
                    newEntity.setName(productDTO.getName());
                    newEntity.setFilingName(productDTO.getFilingName());
                    newEntity.setPrice(productDTO.getPrice());
                    newEntity.setFilingMethods(new ArrayList<>());
                    
                    // Add only unique filing methods
                    if (productDTO.getFilingMethods() != null) {
                        for (FilingMethodDTO methodDTO : productDTO.getFilingMethods()) {
                            UUID methodExternalId = methodDTO.getId();
                            if (!savedMethodIds.contains(methodExternalId)) {
                                FilingMethodEntity methodEntity = filingProductConverter.toMethodEntity(methodDTO);
                                methodEntity.setFilingProduct(newEntity);
                                newEntity.getFilingMethods().add(methodEntity);
                                savedMethodIds.add(methodExternalId);
                            } else {
                                System.out.println("Skipping duplicate FilingMethod: " + methodExternalId);
                            }
                        }
                    }
                    
                    filingProductRepository.save(newEntity);
                    savedProductIds.add(productExternalId);
                }
            }
            
            // Flush all changes at once
            filingProductRepository.flush();
        }

        return apiResponse;
    }

    @Override
    @Transactional(readOnly = true)
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
}

