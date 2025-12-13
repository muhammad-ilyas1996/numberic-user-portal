package com.numbericsuserportal.LlcNorthwest.service;

import com.numbericsuserportal.LlcNorthwest.companies.dto.CompaniesResponseDTO;
import com.numbericsuserportal.LlcNorthwest.companies.dto.CreateCompanyRequestDTO;
import com.numbericsuserportal.LlcNorthwest.companies.dto.UpdateCompanyRequestDTO;
import com.numbericsuserportal.LlcNorthwest.complianceevents.dto.ComplianceEventsResponseDTO;
import com.numbericsuserportal.LlcNorthwest.dto.FilingProductsResponseDTO;
import com.numbericsuserportal.LlcNorthwest.filingmethod.dto.FilingMethodSchemaResponseDTO;
import com.numbericsuserportal.LlcNorthwest.filingmethod.dto.FilingMethodsResponseDTO;
import com.numbericsuserportal.LlcNorthwest.registeredagent.dto.RegisteredAgentProductsResponseDTO;

import java.util.UUID;

public interface CorporateToolsApiService {
    FilingProductsResponseDTO getFilingProducts(String websiteUrl, String jurisdiction, String entityType);
    FilingProductsResponseDTO getFilingProductsOfferings(String companyId, String jurisdiction);
    
    // Companies API methods
    CompaniesResponseDTO getCompanies(Integer limit, Integer offset, String[] names);
    CompaniesResponseDTO getCompanyById(UUID companyId);
    CompaniesResponseDTO createCompanies(CreateCompanyRequestDTO request);
    CompaniesResponseDTO updateCompanies(UpdateCompanyRequestDTO request);
    
    // Filing Methods API methods
    FilingMethodsResponseDTO getFilingMethods(UUID companyId, UUID filingProductId, String jurisdiction);
    FilingMethodSchemaResponseDTO getFilingMethodSchemas(UUID companyId, UUID filingMethodId);
    
    // Compliance Events API methods
    ComplianceEventsResponseDTO getComplianceEvents(
        Integer limit,
        Integer offset,
        String company,
        UUID companyId,
        String startDate,
        String endDate,
        String[] jurisdictions,
        UUID[] jurisdictionIds
    );
    
    // Registered Agent Products API methods
    RegisteredAgentProductsResponseDTO getRegisteredAgentProducts(String url);
}

