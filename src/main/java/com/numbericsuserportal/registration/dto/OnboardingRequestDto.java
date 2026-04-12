package com.numbericsuserportal.registration.dto;

import lombok.Data;

/**
 * Taalr onboarding questionnaire submission.
 * Structured fields map to columns; the whole object is also serialized into {@code onboarding_payload_json} on save.
 * Add new questionnaire keys here — they are stored in JSON without extra DB columns.
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

    // ── Taalr chat / questionnaire (structured); frontend can send all at once after wizard ──

    /** S1 path: e.g. SOLOPRENEUR, BUSINESS_OWNER, TAX_PROFESSIONAL */
    private String onboardingTrack;

    /** S4 */
    private String filingStatus;

    /** S2: "1,3,5" */
    private String incomeSourceCodes;

    /** S5: 1–6 or code */
    private String painPointCode;

    /** B3: operations multi-select */
    private String operationsCodes;

    /** B4 */
    private String taxProRelationship;

    /** B5: STANDARD | PRO */
    private String businessTierChoice;

    /** P1 */
    private String taxProCredential;

    /** P2 */
    private String practiceClientBand;

    /** P2 */
    private String practiceTaxSoftware;

    /** P3 */
    private String practicePainCodes;

    /**
     * Optional full JSON string of all step answers (single payload from frontend state).
     * Example: {"s1":"1","s2":["1","3"],"b1":"2",...}
     */
    private String onboardingAnswersJson;
}
