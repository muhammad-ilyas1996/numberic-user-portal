package com.numbericsuserportal.LlcNorthwest.companies.impl;

import com.numbericsuserportal.LlcNorthwest.companies.dto.CompaniesResponseDTO;
import com.numbericsuserportal.LlcNorthwest.companies.dto.CreateCompanyRequestDTO;
import com.numbericsuserportal.LlcNorthwest.companies.dto.UpdateCompanyRequestDTO;
import com.numbericsuserportal.LlcNorthwest.companies.service.CompanyService;
import com.numbericsuserportal.LlcNorthwest.service.CorporateToolsApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CompanyServiceImpl implements CompanyService {

    @Autowired
    private CorporateToolsApiService corporateToolsApiService;

    @Override
    public CompaniesResponseDTO fetchAndSaveCompanies(Integer limit, Integer offset, String[] names) {
        // Fetch from API only (no database save)
        return corporateToolsApiService.getCompanies(limit, offset, names);
    }


    @Override
    public CompaniesResponseDTO createAndSaveCompanies(CreateCompanyRequestDTO request) {
        // Set default value for duplicate_name_allowed if null
        if (request.getDuplicateNameAllowed() == null) {
            request.setDuplicateNameAllowed(false);
        }
        
        // Validate and prepare each company according to API requirements
        // API Rule: Provide EITHER jurisdictions OR home_state, but NOT both
        if (request.getCompanies() != null) {
            for (CreateCompanyRequestDTO.CompanyInputDTO company : request.getCompanies()) {
                boolean hasJurisdictions = company.getJurisdictions() != null && !company.getJurisdictions().isEmpty();
                boolean hasHomeState = company.getHomeState() != null && !company.getHomeState().trim().isEmpty();
                
                // Validate: At least one must be provided
                if (!hasJurisdictions && !hasHomeState) {
                    throw new IllegalArgumentException(
                        "Either 'jurisdictions' or 'home_state' must be provided for company: " + company.getName()
                    );
                }
                
                // API Rule: If both are provided, use only jurisdictions (remove home_state)
                // OR: If only home_state is provided, remove jurisdictions (set to null)
                if (hasJurisdictions && hasHomeState) {
                    // If both provided, use jurisdictions only (remove home_state)
                    company.setHomeState(null);
                } else if (hasHomeState && !hasJurisdictions) {
                    // If only home_state provided, ensure jurisdictions is null (not empty array)
                    company.setJurisdictions(null);
                }
                // If only jurisdictions provided, home_state is already null (correct)
            }
        }
        
        // Create via API only (no database save)
        return corporateToolsApiService.createCompanies(request);
    }

    @Override
    public CompaniesResponseDTO updateAndSaveCompanies(UpdateCompanyRequestDTO request) {
        // Set default value for duplicate_name_allowed if null
        if (request.getDuplicateNameAllowed() == null) {
            request.setDuplicateNameAllowed(false);
        }
        
        // Validate and prepare each company according to PATCH API requirements
        if (request.getCompanies() != null) {
            for (UpdateCompanyRequestDTO.CompanyUpdateInputDTO company : request.getCompanies()) {
                // PATCH API Rule 1: Either company_id OR company (name) must be provided, but NOT both
                boolean hasCompanyId = company.getCompanyId() != null;
                boolean hasCompanyName = company.getCompany() != null && !company.getCompany().trim().isEmpty();
                
                if (hasCompanyId && hasCompanyName) {
                    throw new IllegalArgumentException(
                        "Either 'company_id' or 'company' (name) must be provided, but not both for company update"
                    );
                }
                if (!hasCompanyId && !hasCompanyName) {
                    throw new IllegalArgumentException(
                        "Either 'company_id' or 'company' (name) must be provided for company update"
                    );
                }
                
                // PATCH API Rule 2: home_state cannot be duplicated in jurisdictions array
                // If both jurisdictions and home_state are provided, remove home_state from jurisdictions array
                boolean hasJurisdictions = company.getJurisdictions() != null && !company.getJurisdictions().isEmpty();
                boolean hasHomeState = company.getHomeState() != null && !company.getHomeState().trim().isEmpty();
                
                if (hasJurisdictions && hasHomeState) {
                    // Remove home_state from jurisdictions array if it exists (no duplicates allowed)
                    company.getJurisdictions().removeIf(j -> j != null && j.equals(company.getHomeState()));
                }
                
                // PATCH API Rule 3: If new jurisdiction is also the home_state, jurisdictions param is not required
                // This is handled by the API itself, we just ensure no duplicates
            }
        }
        
        // Update via API only (no database save)
        return corporateToolsApiService.updateCompanies(request);
    }

}

