package com.numbericsuserportal.federaltax.dto;

import com.numbericsuserportal.federaltax.domain.FederalTaxReturnType;
import lombok.Data;

@Data
public class CreateFederalTaxCaseRequest {
    private FederalTaxReturnType returnType = FederalTaxReturnType.FORM_1040;
    private Integer taxYear;
    private Boolean taxpayerConsent = Boolean.TRUE;
}
