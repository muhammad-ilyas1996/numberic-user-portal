package com.numbericsuserportal.LlcNorthwest.LLCFormation.dto;

import lombok.Data;

@Data
public class UpdateAddonsRequestDTO {
    private Boolean addonEin;
    private Boolean addonScorp;
    private Boolean addonOperatingAgreement;
    private Boolean addonRegisteredAgent;
    private Boolean addonTaxAnalytics;
    private Boolean addonExpenseTracking;
    private Boolean addonBasicAiReporting;
    private Boolean addonCorporateBylaws;
    private String filingSpeed;
}
