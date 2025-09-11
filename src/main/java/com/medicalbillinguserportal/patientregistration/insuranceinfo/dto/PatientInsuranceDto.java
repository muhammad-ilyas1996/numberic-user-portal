package com.medicalbillinguserportal.patientregistration.insuranceinfo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PatientInsuranceDto {
    private Long id;
    private String planName;
    private String policyHolder;
    private String policyNumber;
    private LocalDate expirationDate;
    private String status;
    private String payerName;

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

    private Long patientId;
}
