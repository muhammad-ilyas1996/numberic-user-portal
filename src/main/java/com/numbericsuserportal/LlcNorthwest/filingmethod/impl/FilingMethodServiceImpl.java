package com.numbericsuserportal.LlcNorthwest.filingmethod.impl;

import com.numbericsuserportal.LlcNorthwest.filingmethod.dto.FilingMethodSchemaResponseDTO;
import com.numbericsuserportal.LlcNorthwest.filingmethod.dto.FilingMethodsResponseDTO;
import com.numbericsuserportal.LlcNorthwest.filingmethod.service.FilingMethodService;
import com.numbericsuserportal.LlcNorthwest.service.CorporateToolsApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class FilingMethodServiceImpl implements FilingMethodService {

    @Autowired
    private CorporateToolsApiService corporateToolsApiService;

    @Override
    public FilingMethodsResponseDTO getFilingMethods(UUID companyId, UUID filingProductId, String jurisdiction) {
        return corporateToolsApiService.getFilingMethods(companyId, filingProductId, jurisdiction);
    }

    @Override
    public FilingMethodSchemaResponseDTO getFilingMethodSchemas(UUID companyId, UUID filingMethodId) {
        return corporateToolsApiService.getFilingMethodSchemas(companyId, filingMethodId);
    }
}

