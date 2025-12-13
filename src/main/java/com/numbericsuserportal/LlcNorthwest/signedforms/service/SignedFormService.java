package com.numbericsuserportal.LlcNorthwest.signedforms.service;

import com.numbericsuserportal.LlcNorthwest.signedforms.dto.SignedFormsResponseDTO;

import java.util.UUID;

public interface SignedFormService {
    SignedFormsResponseDTO getSignedForms(UUID filingMethodId, UUID websiteId);
}

