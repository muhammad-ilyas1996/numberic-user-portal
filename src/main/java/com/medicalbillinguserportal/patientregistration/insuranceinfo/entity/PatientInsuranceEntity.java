package com.medicalbillinguserportal.patientregistration.insuranceinfo.entity;

import com.medicalbillinguserportal.commonpersistence.entity.BaseEntity;
import com.medicalbillinguserportal.patientregistration.patientinformation.entity.PatientInfoEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "patient_insurance")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PatientInsuranceEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Existing Insurance (Read-only display data)
    private String planName;
    private String policyHolder;
    private String policyNumber;
    private LocalDate expirationDate;
    private String status;
    private String payerName;

    // Current Policy Details
    private String insuranceStatus;
    private LocalDate effectiveDate;
    private LocalDate currentExpirationDate;
    private String currentPayerName;
    private String payerAddress;
    private String payerClaim;
    private String payerType;
    private String groupNumber;
    private String typeCode;

    private BigDecimal coPay;
    private Integer coIns;
    private Boolean acceptAssignment;

    private String patientRelationship;
    private String insuredFirstName;
    private String insuredMiddleName;
    private String insuredLastName;
    private LocalDate insuredDOB;
    private String insuredSex;

    private String country;
    private String address1;
    private String address2;
    private String city;
    private String state;
    private String homePhone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_info_id")
    private PatientInfoEntity patientInfoEntity;
}
