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

    private String servicePerformer;
    private String authorizationNumber;
    private LocalDate authorizationDate;
    private LocalDate expirationDate;
    private LocalDate requestedDate;
    private String referredBy;
    private String payer;
    private String contactPerson;
    private String phoneNo;
    private String authorizationLimitation;
    private String timeRestriction;
    private String timeRestrictionPer;
    private String placeOfService;
    private String status;
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_info_id")
    private PatientInfoEntity patientInfoEntity;
}
