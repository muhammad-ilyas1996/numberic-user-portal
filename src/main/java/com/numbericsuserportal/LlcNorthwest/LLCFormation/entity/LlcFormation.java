package com.numbericsuserportal.LlcNorthwest.LLCFormation.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "llc_formation")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LlcFormation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "status", nullable = false, length = 32)
    private String status; // DRAFT, READY_FOR_PAYMENT, PAID, SUBMITTED, PROCESSING, APPROVED, FAILED

    @Column(name = "jurisdiction", length = 10)
    private String jurisdiction; // TX

    @Column(name = "entity_type", length = 20)
    private String entityType; // LLC

    @Column(name = "ownership_type", length = 20)
    private String ownershipType; // single|multi

    @Column(name = "operates_in_formation_state")
    private Boolean operatesInFormationState;

    @Column(name = "operating_business_street")
    private String operatingBusinessStreet;

    @Column(name = "operating_business_city")
    private String operatingBusinessCity;

    @Column(name = "operating_business_state", length = 10)
    private String operatingBusinessState;

    @Column(name = "operating_business_zip", length = 10)
    private String operatingBusinessZip;

    @Column(name = "llc_name")
    private String llcName;

    @Column(name = "alt_name")
    private String altName;

    @Column(name = "industry")
    private String industry;

    @Column(name = "business_purpose", columnDefinition = "TEXT")
    private String businessPurpose;

    @Column(name = "name_check_status", length = 32)
    private String nameCheckStatus; // AVAILABLE|TAKEN|UNCHECKED|ERROR

    @Column(name = "name_check_last_checked_at")
    private OffsetDateTime nameCheckLastCheckedAt;

    @Column(name = "owner_first_name")
    private String ownerFirstName;

    @Column(name = "owner_last_name")
    private String ownerLastName;

    @Column(name = "owner_dob")
    private LocalDate ownerDob;

    @Column(name = "owner_ssn_last4_enc")
    private String ownerSsnLast4Enc;

    @Column(name = "ownership_pct")
    private Integer ownershipPct;

    @Column(name = "owner_title")
    private String ownerTitle;

    @Column(name = "management_type", length = 16)
    private String managementType; // member|manager

    @Column(name = "address_same_as_home")
    private Boolean addressSameAsHome;

    @Column(name = "business_street")
    private String businessStreet;

    @Column(name = "business_city")
    private String businessCity;

    @Column(name = "business_state", length = 10)
    private String businessState;

    @Column(name = "business_zip", length = 10)
    private String businessZip;

    @Column(name = "filing_speed", length = 16)
    private String filingSpeed; // standard|expedited|sameday

    @Column(name = "addon_ein")
    private Boolean addonEin;

    @Column(name = "addon_scorp")
    private Boolean addonScorp;

    @Column(name = "addon_operating_agreement")
    private Boolean addonOperatingAgreement;

    @Column(name = "addon_registered_agent")
    private Boolean addonRegisteredAgent;

    @Column(name = "addon_tax_analytics")
    private Boolean addonTaxAnalytics;

    @Column(name = "addon_expense_tracking")
    private Boolean addonExpenseTracking;

    @Column(name = "addon_basic_ai_reporting")
    private Boolean addonBasicAiReporting;

    @Column(name = "addon_corporate_bylaws")
    private Boolean addonCorporateBylaws;

    @Column(name = "numbrics_fee_cents")
    private Integer numbricsFeeCents;

    @Column(name = "state_fee_cents")
    private Integer stateFeeCents;

    @Column(name = "speed_fee_cents")
    private Integer speedFeeCents;

    @Column(name = "ein_fee_cents")
    private Integer einFeeCents;

    @Column(name = "scorp_fee_cents")
    private Integer scorpFeeCents;

    @Column(name = "operating_agreement_fee_cents")
    private Integer operatingAgreementFeeCents;

    @Column(name = "registered_agent_fee_cents")
    private Integer registeredAgentFeeCents;

    @Column(name = "tax_analytics_fee_cents")
    private Integer taxAnalyticsFeeCents;

    @Column(name = "expense_tracking_fee_cents")
    private Integer expenseTrackingFeeCents;

    @Column(name = "basic_ai_reporting_fee_cents")
    private Integer basicAiReportingFeeCents;

    @Column(name = "corporate_bylaws_fee_cents")
    private Integer corporateBylawsFeeCents;

    @Column(name = "total_cents")
    private Integer totalCents;

    @Column(name = "stripe_customer_id")
    private String stripeCustomerId;

    @Column(name = "stripe_payment_intent_id")
    private String stripePaymentIntentId;

    @Column(name = "paid_at")
    private OffsetDateTime paidAt;

    @Column(name = "company_id", length = 64)
    private String companyId;

    @Column(name = "filing_product_id", length = 64)
    private String filingProductId;

    @Column(name = "filing_method_id", length = 64)
    private String filingMethodId;

    @Column(name = "filing_id", length = 64)
    private String filingId;

    @Column(name = "filing_reference")
    private String filingReference;

    @Column(name = "filing_status", length = 255)
    private String filingStatus;

    /** Exact JSON POST body for Corporate Tools POST /shopping-cart (preferred over server-side fallback). */
    @Column(name = "northwest_shopping_cart_json", columnDefinition = "TEXT")
    private String northwestShoppingCartJson;

    /** Set once NW wholesale checkout succeeds; used for Stripe webhook idempotency. */
    @Column(name = "northwest_checkout_completed_at")
    private OffsetDateTime northwestCheckoutCompletedAt;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    void prePersist() {
        OffsetDateTime now = OffsetDateTime.now();
        if (createdAt == null) createdAt = now;
        updatedAt = now;
        if (status == null) status = "DRAFT";
        if (entityType == null) entityType = "LLC";
        if (nameCheckStatus == null) nameCheckStatus = "UNCHECKED";
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}

