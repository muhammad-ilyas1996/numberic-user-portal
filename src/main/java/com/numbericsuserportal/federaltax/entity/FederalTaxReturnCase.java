package com.numbericsuserportal.federaltax.entity;

import com.numbericsuserportal.federaltax.domain.ClientTaxStatus;
import com.numbericsuserportal.federaltax.domain.FederalTaxReturnType;
import com.numbericsuserportal.federaltax.domain.TaxServiceOption;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "federal_tax_return_cases")
@Data
public class FederalTaxReturnCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_case_id", nullable = false, unique = true, length = 40)
    private String publicCaseId;

    @Column(name = "taxpayer_user_id", nullable = false)
    private Long taxpayerUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "return_type", nullable = false, length = 32)
    private FederalTaxReturnType returnType;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_option", length = 32)
    private TaxServiceOption serviceOption;

    @Enumerated(EnumType.STRING)
    @Column(name = "client_status", nullable = false, length = 40)
    private ClientTaxStatus clientStatus = ClientTaxStatus.INTAKE;

    @Column(name = "tax_year", nullable = false)
    private Integer taxYear;

    /** Structured organizer JSON (1040 / Sch C fields). */
    @Lob
    @Column(name = "organizer_json", columnDefinition = "LONGTEXT")
    private String organizerJson;

    @Lob
    @Column(name = "extracted_json", columnDefinition = "LONGTEXT")
    private String extractedJson;

    @Lob
    @Column(name = "validation_json", columnDefinition = "LONGTEXT")
    private String validationJson;

    @Lob
    @Column(name = "missing_items_json", columnDefinition = "LONGTEXT")
    private String missingItemsJson;

    /** Internal only — never expose to client APIs. */
    @Column(name = "assigned_preparer_user_id")
    private Long assignedPreparerUserId;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @Column(name = "reviewer_user_id")
    private Long reviewerUserId;

    @Column(name = "reviewer_approved", nullable = false)
    private boolean reviewerApproved = false;

    @Column(name = "reviewer_approved_at")
    private LocalDateTime reviewerApprovedAt;

    @Column(name = "clarification_open", nullable = false)
    private boolean clarificationOpen = false;

    @Column(name = "clarification_note", length = 1000)
    private String clarificationNote;

    @Column(name = "export_package_path", length = 500)
    private String exportPackagePath;

    @Column(name = "form_8879_signed", nullable = false)
    private boolean form8879Signed = false;

    @Column(name = "form_8879_signed_at")
    private LocalDateTime form8879SignedAt;

    @Column(name = "spouse_8879_required", nullable = false)
    private boolean spouse8879Required = false;

    @Column(name = "spouse_8879_signed", nullable = false)
    private boolean spouse8879Signed = false;

    @Column(name = "taxpayer_consent", nullable = false)
    private boolean taxpayerConsent = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
        if (clientStatus == null) {
            clientStatus = ClientTaxStatus.INTAKE;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
