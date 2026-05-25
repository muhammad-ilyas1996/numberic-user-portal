package com.numbericsuserportal.invoice.dto;

import lombok.Data;

@Data
public class NmiMerchantDashboardDto {

    private Long userId;
    private Boolean paymentReady;
    private String nextAction;
    private String message;
    private MerchantNmiConfigDto nmiConfig;
    private NmiMerchantOnboardingStatusDto onboarding;
}
