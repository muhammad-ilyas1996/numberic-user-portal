package com.numbericsuserportal.LlcNorthwest.LLCFormation.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "llc_formation_state_catalog", uniqueConstraints = {
        @UniqueConstraint(name = "uk_llc_formation_state_catalog_code", columnNames = {"state_code"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LlcFormationStateCatalog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "state_name", nullable = false, length = 64)
    private String stateName;

    @Column(name = "state_code", nullable = false, length = 10)
    private String stateCode;

    @Column(name = "filing_fee_cents", nullable = false)
    private Integer filingFeeCents;

    @Column(name = "annual_report_fee", length = 128)
    private String annualReportFee;

    @Column(name = "processing_time", length = 64)
    private String processingTime;

    @Column(name = "speed", length = 16)
    private String speed;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "nw_service_fee_cents", nullable = false)
    private Integer nwServiceFeeCents = 3900;

    @Column(name = "nw_ra_year1_cents", nullable = false)
    private Integer nwRaYear1Cents = 0;

    @Column(name = "nw_ra_renewal_cents", nullable = false)
    private Integer nwRaRenewalCents = 12500;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    void prePersist() {
        OffsetDateTime now = OffsetDateTime.now();
        if (createdAt == null) createdAt = now;
        updatedAt = now;
        if (active == null) active = true;
        if (nwServiceFeeCents == null) nwServiceFeeCents = 3900;
        if (nwRaYear1Cents == null) nwRaYear1Cents = 0;
        if (nwRaRenewalCents == null) nwRaRenewalCents = 12500;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
