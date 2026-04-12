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

    private String onboardingTrack;
    private String filingStatus;
    private String incomeSourceCodes;
    private String painPointCode;
    private String operationsCodes;
    private String taxProRelationship;
    private String businessTierChoice;
    private String taxProCredential;
    private String practiceClientBand;
    private String practiceTaxSoftware;
    private String practicePainCodes;
    private String onboardingAnswersJson;

    /** Full last submit payload as JSON (same as stored in DB); parse on client for dynamic questionnaire fields. */
    private String onboardingPayloadJson;

    /** True if user has submitted onboarding (has profile with at least business name or key fields). */
    private Boolean completed;
}
