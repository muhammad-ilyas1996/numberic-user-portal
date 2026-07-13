package com.numbericsuserportal.stripeintegration.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Admin-editable subscription plan catalog (public pricing page source of truth).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "subscription_plan_catalog")
public class SubscriptionPlanCatalogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Stable code: SOLOPRENEUR, BUSINESS_OWNER, ACCOUNTANT_PRO, ... */
    @Column(name = "plan_code", nullable = false, unique = true, length = 40)
    private String planCode;

    @Column(name = "display_name", nullable = false, length = 120)
    private String displayName;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "audience", length = 200)
    private String audience;

    /** Base monthly price in cents (e.g. 1900 = $19). */
    @Column(name = "amount_cents", nullable = false)
    private Long amountCents;

    /** Optional hybrid add-on monthly cents (e.g. 1000 = $10). */
    @Column(name = "hybrid_addon_cents", nullable = false)
    private Long hybridAddonCents = 1000L;

    @Column(name = "trial_days", nullable = false)
    private Integer trialDays = 7;

    /** Role assigned on subscribe, e.g. NUMBRICS_BUSINESS_OWNER */
    @Column(name = "default_role_code", nullable = false, length = 80)
    private String defaultRoleCode;

    /** If true, billing multiplies by seat count (Accountant Pro). */
    @Column(name = "per_seat", nullable = false)
    private Boolean perSeat = false;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @Column(name = "is_active", nullable = false)
    private Boolean active = true;

    @Column(name = "features_json", columnDefinition = "TEXT")
    private String featuresJson;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (hybridAddonCents == null) {
            hybridAddonCents = 1000L;
        }
        if (trialDays == null) {
            trialDays = 7;
        }
        if (perSeat == null) {
            perSeat = false;
        }
        if (active == null) {
            active = true;
        }
        if (sortOrder == null) {
            sortOrder = 0;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public double amountDollars() {
        return amountCents == null ? 0.0 : amountCents / 100.0;
    }

    public double hybridAddonDollars() {
        return hybridAddonCents == null ? 0.0 : hybridAddonCents / 100.0;
    }
}
