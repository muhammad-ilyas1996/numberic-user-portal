package com.numbericsuserportal.LlcNorthwest.companies.impl;

import com.numbericsuserportal.LlcNorthwest.companies.dto.CompaniesResponseDTO;
import com.numbericsuserportal.LlcNorthwest.companies.dto.CompanyDTO;
import com.numbericsuserportal.LlcNorthwest.companies.dto.CreateCompanyRequestDTO;
import com.numbericsuserportal.LlcNorthwest.companies.dto.UpdateCompanyRequestDTO;
import com.numbericsuserportal.LlcNorthwest.companies.entity.UserFormationCompany;
import com.numbericsuserportal.LlcNorthwest.companies.repo.UserFormationCompanyRepository;
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
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class CompanyServiceImpl implements CompanyService {

    @Autowired
    private CorporateToolsApiService corporateToolsApiService;

    @Autowired
    private UserDataScopeService userDataScopeService;

    @Autowired
    private UserFormationCompanyRepository userFormationCompanyRepository;

    @Override
    @Transactional(readOnly = true)
    public CompaniesResponseDTO fetchAndSaveCompanies(User user, Integer limit, Integer offset, String[] names) {
        UserDataScopeContext scope = userDataScopeService.resolve(user);
        if (scope.isPlatformWideDataAccess()) {
            return corporateToolsApiService.getCompanies(limit, offset, names);
        }

        List<UserFormationCompany> links = userFormationCompanyRepository.findByUserId(user.getUserId());
        CompaniesResponseDTO response = new CompaniesResponseDTO();
        response.setSuccess(true);
        response.setTimestamp(OffsetDateTime.now().toString());

        if (links.isEmpty()) {
            response.setResult(List.of());
            return response;
        }

        List<CompanyDTO> all = new ArrayList<>();
        for (UserFormationCompany link : links) {
            try {
                CompaniesResponseDTO one = corporateToolsApiService.getCompanyById(UUID.fromString(link.getCompanyId()));
                if (one.getResult() != null) {
                    all.addAll(one.getResult());
                }
            } catch (Exception ignored) {
                // skip broken or removed company ids
            }
        }

        int from = offset != null && offset >= 0 ? offset : 0;
        int lim = limit != null && limit > 0 ? limit : all.size();
        if (from >= all.size()) {
            response.setResult(List.of());
        } else {
            int to = Math.min(from + lim, all.size());
            response.setResult(all.subList(from, to));
        }
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

        if (user.getUserId() != null && response.getResult() != null) {
            Date now = new Date();
            for (CompanyDTO c : response.getResult()) {
                if (c.getId() != null) {
                    String cid = c.getId().toString();
                    if (!userFormationCompanyRepository.existsByUserIdAndCompanyId(user.getUserId(), cid)) {
                        UserFormationCompany row = new UserFormationCompany();
                        row.setUserId(user.getUserId());
                        row.setCompanyId(cid);
                        row.setLinkedAt(now);
                        userFormationCompanyRepository.save(row);
                    }
                }
            }
        }

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

        if (!scope.isPlatformWideDataAccess() && request.getCompanies() != null) {
            for (UpdateCompanyRequestDTO.CompanyUpdateInputDTO c : request.getCompanies()) {
                if (c.getCompanyId() == null) {
                    throw new AccessDeniedException("company_id is required to update a company linked to your account");
                }
                if (!userFormationCompanyRepository.existsByUserIdAndCompanyId(user.getUserId(), c.getCompanyId().toString())) {
                    throw new AccessDeniedException("You do not have access to this company");
                }
            }
        }

        return corporateToolsApiService.updateCompanies(request);
    }
}
