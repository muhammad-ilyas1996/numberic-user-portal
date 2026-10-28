package com.numbericsuserportal.federaltax.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Phase 1 e-signature record for Form 8879.
 * Provider field documents the compliance approach (e.g. INTERNAL_AUDIT_TRAIL / DOCUSIGN).
 */
@Entity
@Table(name = "form_8879_authorizations")
@Data
public class Form8879Authorization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "case_id", nullable = false)
    private Long caseId;

    @Column(name = "signer_role", nullable = false, length = 32)
    private String signerRole; // TAXPAYER | SPOUSE

    @Column(name = "signer_user_id")
    private Long signerUserId;

    @Column(name = "signer_name", length = 160)
    private String signerName;

    @Column(name = "consent_text", length = 2000)
    private String consentText;

    @Column(name = "signature_method", length = 64)
    private String signatureMethod;

    @Column(name = "e_sign_provider", length = 64)
    private String eSignProvider = "INTERNAL_AUDIT_TRAIL";

    @Column(name = "ip_address", length = 64)
    private String ipAddress;

    @Column(name = "user_agent", length = 512)
    private String userAgent;

    @Column(name = "signed_at", nullable = false)
    private LocalDateTime signedAt;

    @Column(name = "auth_token_hash", length = 128)
    private String authTokenHash;
}
