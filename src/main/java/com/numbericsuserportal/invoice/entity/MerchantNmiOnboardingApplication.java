package com.numbericsuserportal.invoice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Tracks a merchant boarding application submitted through Numbrics to the NMI reseller flow.
 */
@Entity
@Table(name = "merchant_nmi_onboarding_application")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MerchantNmiOnboardingApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(length = 64, nullable = false)
    private String status;

    @Column(name = "business_name", length = 255)
    private String businessName;

    @Column(name = "legal_name", length = 255)
    private String legalName;

    @Column(name = "contact_email", length = 255)
    private String contactEmail;

    @Column(name = "nmi_application_id", length = 128)
    private String nmiApplicationId;

    @Column(name = "nmi_merchant_id", length = 128)
    private String nmiMerchantId;

    @Column(name = "declined_reason", length = 1000)
    private String declinedReason;

    @Column(name = "safe_response_json", columnDefinition = "TEXT")
    private String safeResponseJson;

    @Column(name = "created_on")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdOn;

    @Column(name = "updated_on")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedOn;

    @Column(name = "submitted_on")
    @Temporal(TemporalType.TIMESTAMP)
    private Date submittedOn;

    @Column(name = "approved_on")
    @Temporal(TemporalType.TIMESTAMP)
    private Date approvedOn;
}
