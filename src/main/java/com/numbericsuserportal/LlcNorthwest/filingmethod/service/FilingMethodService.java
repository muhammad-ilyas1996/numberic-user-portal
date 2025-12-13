package com.numbericsuserportal.LlcNorthwest.filingmethod.service;

import com.numbericsuserportal.LlcNorthwest.filingmethod.dto.FilingMethodSchemaResponseDTO;
import com.numbericsuserportal.LlcNorthwest.filingmethod.dto.FilingMethodsResponseDTO;

import java.util.UUID;

public interface FilingMethodService {
    FilingMethodsResponseDTO getFilingMethods(UUID companyId, UUID filingProductId, String jurisdiction);
    FilingMethodSchemaResponseDTO getFilingMethodSchemas(UUID companyId, UUID filingMethodId);
}

