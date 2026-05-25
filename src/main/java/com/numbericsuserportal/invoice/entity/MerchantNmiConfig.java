package com.numbericsuserportal.invoice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Per-merchant (user) NMI credentials. Merchant onboarded on NMI portal, then configures here via Settings.
 */
@Entity
@Table(name = "merchant_nmi_config", uniqueConstraints = @UniqueConstraint(columnNames = "user_id"))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MerchantNmiConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    /** api_key or username_password */
    @Column(name = "auth_method", length = 32)
    private String authMethod = "api_key";

    @Column(name = "security_key", length = 256)
    private String securityKey;

    @Column(name = "nmi_username", length = 128)
    private String nmiUsername;

    @Column(name = "nmi_password", length = 256)
    private String nmiPassword;

    /** NMI transaction URL (optional; default from app config if null) */
    @Column(name = "transaction_url", length = 512)
    private String transactionUrl;

    /** Merchant/gateway id returned by NMI reseller boarding, when available. */
    @Column(name = "nmi_merchant_id", length = 128)
    private String nmiMerchantId;

    /** Latest onboarding status for this merchant (PENDING, APPROVED, ACTIVE, DECLINED, etc.). */
    @Column(name = "boarding_status", length = 64)
    private String boardingStatus;

    /** Reseller/boarding application id used for status polling or webhook updates. */
    @Column(name = "boarding_application_id", length = 128)
    private String boardingApplicationId;

    @Column(name = "approved_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date approvedAt;
}
