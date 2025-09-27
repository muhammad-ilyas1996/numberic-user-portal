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
    private String insuranceLevel;
    private String insuranceStatus;
    private LocalDate effectiveDate;
    private LocalDate expirationDate;
    private String payerName;
    private String payerAddress;
    private String payerClass;
    private String payerType;
    private String planName;
    private String groupNo;
    private String insuranceTypeCode;
    private String insurancesID;
    private String coPay;
    private String coIns;
    private String acceptAssignment;

    //Insurance Info
    private String patientRelationship;
    private String insuredFirstName;
    private String middleName;
    private String insuredLastName;
    private String insuredDob;
    private String insuredSex;
    private String country;
    private String address1;
    private String address2;
    private String city;
    private String state;
    private String zip;
    private String homePhone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_info_id")
    private PatientInfoEntity patientInfoEntity;
}
