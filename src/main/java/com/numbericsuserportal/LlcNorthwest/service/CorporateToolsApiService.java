package com.numbericsuserportal.LlcNorthwest.service;

import com.numbericsuserportal.LlcNorthwest.companies.dto.CompaniesResponseDTO;
import com.numbericsuserportal.LlcNorthwest.companies.dto.CreateCompanyRequestDTO;
import com.numbericsuserportal.LlcNorthwest.companies.dto.UpdateCompanyRequestDTO;
import com.numbericsuserportal.LlcNorthwest.dto.FilingProductsResponseDTO;

import java.util.UUID;

public interface CorporateToolsApiService {
    FilingProductsResponseDTO getFilingProducts(String websiteUrl, String jurisdiction, String entityType);
    FilingProductsResponseDTO getFilingProductsOfferings(String companyId, String jurisdiction);
    
    // Companies API methods
    CompaniesResponseDTO getCompanies(Integer limit, Integer offset, String[] names);
    CompaniesResponseDTO getCompanyById(UUID companyId);
    CompaniesResponseDTO createCompanies(CreateCompanyRequestDTO request);
    CompaniesResponseDTO updateCompanies(UpdateCompanyRequestDTO request);
}

