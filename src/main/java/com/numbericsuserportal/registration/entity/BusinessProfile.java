package com.numbericsuserportal.registration.entity;

import com.numbericsuserportal.commonpersistence.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "business_profiles")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class BusinessProfile extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "business_profile_id")
    private Long businessProfileId;
    
    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;
    
    @Column(name = "company_name", length = 255)
    private String companyName; // From step 1
    
    @Column(name = "business_legal_name", length = 255)
    private String businessLegalName;
    
    @Column(name = "dba", length = 255)
    private String dba;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "business_type", length = 50)
    private BusinessType businessType;
    
    @Column(name = "industry", length = 100)
    private String industry;
    
    @Column(name = "naics_code", length = 20)
    private String naicsCode;
    
    @Column(name = "ein", length = 20)
    private String ein;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "tax_classification", length = 50)
    private TaxClassification taxClassification;
    
    @Column(name = "website", length = 255)
    private String website;
    
    @Column(name = "business_address_id")
    private Long businessAddressId;
    
    @Column(name = "mailing_address_id")
    private Long mailingAddressId;
    
    @Column(name = "mailing_address_same")
    private Boolean mailingAddressSame = true;

    /** Taalr onboarding fields (update same row, never delete) */
    @Column(name = "entity_type", length = 50)
    private String entityType;
    @Column(name = "registration_state", length = 100)
    private String registrationState;
    @Column(name = "revenue_last_year", length = 100)
    private String revenueLastYear;
    @Column(name = "revenue_expected", length = 100)
    private String revenueExpected;
    @Column(name = "collects_sales_tax", length = 50)
    private String collectsSalesTax;
    @Column(name = "multi_state", length = 50)
    private String multiState;
    @Column(name = "has_employees", length = 50)
    private String hasEmployees;
    @Column(name = "team_size", length = 100)
    private String teamSize;
    @Column(name = "files_quarterly_taxes", length = 50)
    private String filesQuarterlyTaxes;
    @Column(name = "sales_tax_frequency", length = 50)
    private String salesTaxFrequency;
    @Column(name = "tax_filings_up_to_date", length = 50)
    private String taxFilingsUpToDate;
    @Column(name = "accounting_software", length = 100)
    private String accountingSoftware;
    @Column(name = "connect_bank_accounts", length = 50)
    private String connectBankAccounts;
    @Column(name = "goals", length = 500)
    private String goals;
    @Column(name = "location", length = 255)
    private String location;

    /** S1: SOLOPRENEUR | BUSINESS_OWNER | TAX_PROFESSIONAL (or client-defined codes). */
    @Column(name = "onboarding_track", length = 64)
    private String onboardingTrack;

    /** S4 filing status. */
    @Column(name = "filing_status", length = 64)
    private String filingStatus;

    /** S2: comma-separated option indices e.g. "1,3,5". */
    @Column(name = "income_source_codes", length = 512)
    private String incomeSourceCodes;

    /** S5 pain point option 1–6 or code. */
    @Column(name = "pain_point_code", length = 32)
    private String painPointCode;

    /** B3 operations multi-select codes. */
    @Column(name = "operations_codes", length = 512)
    private String operationsCodes;

    /** B4: CPA relationship choice. */
    @Column(name = "tax_pro_relationship", length = 64)
    private String taxProRelationship;

    /** B5: STANDARD | PRO or display string. */
    @Column(name = "business_tier_choice", length = 64)
    private String businessTierChoice;

    /** P1 Tax Professional credential. */
    @Column(name = "tax_pro_credential", length = 64)
    private String taxProCredential;

    /** P2 client count band. */
    @Column(name = "practice_client_band", length = 64)
    private String practiceClientBand;

    /** P2 tax software. */
    @Column(name = "practice_tax_software", length = 128)
    private String practiceTaxSoftware;

    /** P3 practice pain multi-select codes. */
    @Column(name = "practice_pain_codes", length = 512)
    private String practicePainCodes;

    /**
     * Full onboarding snapshot from the frontend (one POST after chat wizard): steps, raw labels, extras.
     * Use together with structured columns above; JSON is optional overflow / audit.
     */
    @Column(name = "onboarding_answers_json", columnDefinition = "LONGTEXT")
    private String onboardingAnswersJson;

    /**
     * Canonical JSON of the entire last {@code POST /api/onboarding} body (serialized {@link com.numbericsuserportal.registration.dto.OnboardingRequestDto}).
     * Add new fields to the DTO only — no DB migration needed to retain them here. Use for display / future steps.
     */
    @Column(name = "onboarding_payload_json", columnDefinition = "LONGTEXT")
    private String onboardingPayloadJson;

    public enum BusinessType {
        llc, corp, sole_proprietor, partnership, nonprofit, other
    }
    
    public enum TaxClassification {
        disregarded_single_member, partnership, c_corp, s_corp, nonprofit
    }
}

