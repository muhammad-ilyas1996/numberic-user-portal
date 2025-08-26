package com.medicalbillinguserportal.patientregistration.authorization.entity;

import com.medicalbillinguserportal.commonpersistence.entity.BaseEntity;
import com.medicalbillinguserportal.patientregistration.patientinformation.entity.PatientInfoEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "patient_authorization")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PatientAuthorizationEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String authorizationNumber;

    private String status;

    private LocalDate requestedDate;

    private String currentPayer;

    private Integer authorizedQty;

    private BigDecimal authAmountSum;

    private LocalDate effectiveDate;

    private LocalDate expirationDate;

    private String approvalType;

    private Boolean test;

    private LocalDate date;

    private String claimNumber;

    private String chapter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_info_id")
    private PatientInfoEntity patientInfoEntity;
}
