package com.numbericsuserportal.LlcNorthwest.companies.service;

import com.numbericsuserportal.LlcNorthwest.companies.dto.CompaniesResponseDTO;
import com.numbericsuserportal.LlcNorthwest.companies.dto.CreateCompanyRequestDTO;
import com.numbericsuserportal.LlcNorthwest.companies.dto.UpdateCompanyRequestDTO;
import com.numbericsuserportal.usermanagement.domain.User;

public interface CompanyService {

    CompaniesResponseDTO fetchAndSaveCompanies(User user, Integer limit, Integer offset, String[] names);

    CompaniesResponseDTO createAndSaveCompanies(User user, CreateCompanyRequestDTO request);

    CompaniesResponseDTO updateAndSaveCompanies(User user, UpdateCompanyRequestDTO request);
}
