package com.numbericsuserportal.LlcNorthwest.companies.service;

import com.numbericsuserportal.LlcNorthwest.companies.dto.CompaniesResponseDTO;
import com.numbericsuserportal.LlcNorthwest.companies.dto.CreateCompanyRequestDTO;
import com.numbericsuserportal.LlcNorthwest.companies.dto.UpdateCompanyRequestDTO;

import java.util.UUID;

public interface CompanyService {
    CompaniesResponseDTO fetchAndSaveCompanies(Integer limit, Integer offset, String[] names);
    CompaniesResponseDTO getCompaniesFromDatabase();
    CompaniesResponseDTO getCompanyByIdFromDatabase(UUID companyId);
    CompaniesResponseDTO createAndSaveCompanies(CreateCompanyRequestDTO request);
    CompaniesResponseDTO updateAndSaveCompanies(UpdateCompanyRequestDTO request);
}

