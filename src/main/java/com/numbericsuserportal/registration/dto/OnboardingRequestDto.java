package com.numbericsuserportal.registration.dto;

import lombok.Data;

/**
 * Taalr onboarding questionnaire submission.
 * Maps to 15 questions; saved/updated in business_profiles (same row, never delete).
 */
@Data
public class OnboardingRequestDto {

    private String businessName;

    private String entityType;           // LLC, S-Corp, Sole Prop, etc.
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
}
