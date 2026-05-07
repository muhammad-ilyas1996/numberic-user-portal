package com.numbericsuserportal.LlcNorthwest.companies.impl;
import com.numbericsuserportal.LlcNorthwest.companies.dto.CompaniesResponseDTO;
import com.numbericsuserportal.LlcNorthwest.companies.dto.CompanyDTO;
import com.numbericsuserportal.LlcNorthwest.companies.dto.CreateCompanyRequestDTO;
import com.numbericsuserportal.LlcNorthwest.companies.dto.UpdateCompanyRequestDTO;
import com.numbericsuserportal.LlcNorthwest.companies.service.CompanyService;
import com.numbericsuserportal.LlcNorthwest.service.CorporateToolsApiService;
import com.numbericsuserportal.usermanagement.domain.User;
import com.numbericsuserportal.usermanagement.service.UserDataScopeContext;
import com.numbericsuserportal.usermanagement.service.UserDataScopeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class CompanyServiceImpl implements CompanyService {

    @Autowired
    private CorporateToolsApiService corporateToolsApiService;

    @Autowired
    private UserDataScopeService userDataScopeService;

    @Override
    @Transactional(readOnly = true)
    public CompaniesResponseDTO fetchAndSaveCompanies(User user, Integer limit, Integer offset, String[] names) {
        UserDataScopeContext scope = userDataScopeService.resolve(user);
        if (scope.isPlatformWideDataAccess()) {
            return corporateToolsApiService.getCompanies(limit, offset, names);
        }

        CompaniesResponseDTO response = new CompaniesResponseDTO();
        response.setSuccess(true);
        response.setTimestamp(OffsetDateTime.now().toString());

        // Requirement: Do not persist/link companies in the existing LlcNorthwest module.
        // For non-admin users, company access should be derived from LLCFormation records instead.
        // Until LLCFormation integration is fully wired here, return empty list for non-platform-wide access.
        response.setResult(List.of());
        return response;
    }

    @Override
    @Transactional
    public CompaniesResponseDTO createAndSaveCompanies(User user, CreateCompanyRequestDTO request) {
        if (request.getDuplicateNameAllowed() == null) {
            request.setDuplicateNameAllowed(false);
        }

        if (request.getCompanies() != null) {
            for (CreateCompanyRequestDTO.CompanyInputDTO company : request.getCompanies()) {
                boolean hasJurisdictions = company.getJurisdictions() != null && !company.getJurisdictions().isEmpty();
                boolean hasHomeState = company.getHomeState() != null && !company.getHomeState().trim().isEmpty();

                if (!hasJurisdictions && !hasHomeState) {
                    throw new IllegalArgumentException(
                            "Either 'jurisdictions' or 'home_state' must be provided for company: " + company.getName()
                    );
                }

                if (hasJurisdictions && hasHomeState) {
                    company.setHomeState(null);
                } else if (hasHomeState && !hasJurisdictions) {
                    company.setJurisdictions(null);
                }
            }
        }

        CompaniesResponseDTO response = corporateToolsApiService.createCompanies(request);
        return response;
    }

    @Override
    public CompaniesResponseDTO updateAndSaveCompanies(User user, UpdateCompanyRequestDTO request) {
        UserDataScopeContext scope = userDataScopeService.resolve(user);

        if (request.getDuplicateNameAllowed() == null) {
            request.setDuplicateNameAllowed(false);
        }

        if (request.getCompanies() != null) {
            for (UpdateCompanyRequestDTO.CompanyUpdateInputDTO company : request.getCompanies()) {
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

                boolean hasJurisdictions = company.getJurisdictions() != null && !company.getJurisdictions().isEmpty();
                boolean hasHomeState = company.getHomeState() != null && !company.getHomeState().trim().isEmpty();

                if (hasJurisdictions && hasHomeState) {
                    company.getJurisdictions().removeIf(j -> j != null && j.equals(company.getHomeState()));
                }
            }
        }

        if (!scope.isPlatformWideDataAccess()) {
            throw new AccessDeniedException("Company updates are restricted to platform-wide access in this module");
        }

        return corporateToolsApiService.updateCompanies(request);
    }
}
