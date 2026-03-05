package com.numbericsuserportal.invoice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
}
