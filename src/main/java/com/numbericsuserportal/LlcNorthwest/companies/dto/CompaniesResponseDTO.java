package com.numbericsuserportal.LlcNorthwest.companies.dto;

import lombok.Data;
import java.util.List;

@Data
public class CompaniesResponseDTO {
    private Boolean success;
    private String timestamp;
    private List<CompanyDTO> result;
}

