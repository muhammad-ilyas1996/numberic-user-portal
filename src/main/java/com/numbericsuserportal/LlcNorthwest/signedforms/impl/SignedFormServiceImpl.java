package com.numbericsuserportal.LlcNorthwest.signedforms.impl;

import com.numbericsuserportal.LlcNorthwest.signedforms.dto.SignedFormsResponseDTO;
import com.numbericsuserportal.LlcNorthwest.signedforms.service.SignedFormService;
import com.numbericsuserportal.LlcNorthwest.service.CorporateToolsApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class SignedFormServiceImpl implements SignedFormService {

    @Autowired
    private CorporateToolsApiService corporateToolsApiService;

    @Override
    public SignedFormsResponseDTO getSignedForms(UUID filingMethodId, UUID websiteId) {
        return corporateToolsApiService.getSignedForms(filingMethodId, websiteId);
    }
}

