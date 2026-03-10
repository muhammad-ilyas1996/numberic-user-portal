package com.numbericsuserportal.registration.dto;

import lombok.Data;

/**
 * Onboarding profile for GET (pre-fill form / status).
 */
@Data
public class OnboardingResponseDto {

    private Long businessProfileId;
    private Long userId;

    private String businessName;
    private String companyName;
    private String businessLegalName;
    private String entityType;
    private String registrationState;
    private String industry;
    private String revenueLastYear;
    private String revenueExpected;
    private String collectsSalesTax;
    private String multiState;
    private String hasEmployees;
    private String teamSize;
    private String filesQuarterlyTaxes;
    private String salesTaxFrequency;
    private String taxFilingsUpToDate;
    private String accountingSoftware;
    private String connectBankAccounts;
    private String goals;
    private String location;

    /** True if user has submitted onboarding (has profile with at least business name or key fields). */
    private Boolean completed;
}
