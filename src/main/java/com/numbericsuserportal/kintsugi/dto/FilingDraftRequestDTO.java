package com.numbericsuserportal.kintsugi.dto;

import lombok.Data;

@Data
public class FilingDraftRequestDTO {
    private String filingId;
    private String stateCode;
    private String category;
    private String subcategory;
    private Double taxableAmount;
    private Double exemptAmount;
    private String stateTaxAccountId;
    private Boolean registrationRequired;
}
