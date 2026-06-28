package com.numbericsuserportal.kintsugi.dto;

import lombok.Data;

@Data
public class FilingFlowSelectionDTO {
    private String filingId;
    private String stateCode;
    private String category;
    private String subcategory;
    private String stateTaxAccountId;
    private Boolean registrationRequired;
}
