package com.numbericsuserportal.kintsugi.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "sales_tax_filings")
@Getter
@Setter
public class SalesTaxFilingEntity {

    public enum FilingStatus {
        DRAFT, PENDING_PAYMENT, FILED, CONFIRMED, OVERDUE
    }

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "state_code", nullable = false, length = 2)
    private String stateCode;

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "category", length = 50)
    private String category;

    @Column(name = "subcategory", length = 100)
    private String subcategory;

    @Column(name = "total_revenue_cents")
    private Long totalRevenueCents = 0L;

    @Column(name = "taxable_amount_cents")
    private Long taxableAmountCents = 0L;

    @Column(name = "exempt_amount_cents")
    private Long exemptAmountCents = 0L;

    @Column(name = "exempt_categories", columnDefinition = "JSON")
    private String exemptCategoriesJson;

    @Column(name = "taxable_categories", columnDefinition = "JSON")
    private String taxableCategoriesJson;

    @Column(name = "kintsugi_estimate", columnDefinition = "JSON")
    private String kintsugiEstimateJson;

    @Column(name = "jurisdiction_breakdown", columnDefinition = "JSON")
    private String jurisdictionBreakdownJson;

    @Column(name = "kintsugi_filing_response", columnDefinition = "JSON")
    private String kintsugiFilingResponseJson;

    @Column(name = "business_type", length = 30)
    private String businessType;

    @Column(name = "flow_completed")
    private Boolean flowCompleted = false;

    @Column(name = "tax_collected_cents")
    private Long taxCollectedCents = 0L;

    @Column(name = "savings_amount_cents")
    private Long savingsAmountCents = 0L;

    @Column(name = "tax_rate")
    private Double taxRate;

    @Column(name = "filing_fee_cents")
    private Long filingFeeCents = 999L;

    @Column(name = "registration_fee_cents")
    private Long registrationFeeCents = 0L;

    @Column(name = "filing_status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private FilingStatus filingStatus = FilingStatus.DRAFT;

    @Column(name = "filed_at")
    private LocalDateTime filedAt;

    @Column(name = "confirmation_num", length = 100)
    private String confirmationNum;

    @Column(name = "numbrics_filing_id", length = 100)
    private String numbricsFilingId;

    @Column(name = "kintsugi_filing_id", length = 100)
    private String kintsugiFilingId;

    @Column(name = "stripe_payment_intent_id", length = 100)
    private String stripePaymentIntentId;

    @Column(name = "state_tax_account_id", length = 100)
    private String stateTaxAccountId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
