package com.medicalbillinguserportal.patientregistration.insuranceinfo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PatientInsuranceDto {
    private Long id;
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

    // Insurance Info
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

//Audit
    private Date createdOn;
    private Date modifiedOn;
    private String createdBy;
    private String modifiedBy;
    private Boolean isActive;
    private Long patientId;
}
