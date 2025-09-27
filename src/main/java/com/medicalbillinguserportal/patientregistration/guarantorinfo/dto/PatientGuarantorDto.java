package com.medicalbillinguserportal.patientregistration.guarantorinfo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PatientGuarantorDto {
    private Long id;

    private String relationToPatient;
    private String firstName;
    private String middleName;
    private String lastNameOrOrg;
    private LocalDate dob;
    private String sex;

    private String race;
    private String ethnicity;
    private String language;
    private String country;

    private String ssn;

    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String zip;
    private String maritalStatus;
    private String pregnant;
    private String employmentStatus;

    private String employerName;
    private String employerPhone;
    private String employerAddress1;
    private String employerAddress2;
    private String employerCity;
    private String employerState;
    private String employerZip;
    private String employerOccupation;
    private String employerMultipleBirth;
    private String employerBirthOrder;
    private String employerMothersMaidenName;

    // Guarantor Contact Info
    private String homePhone;
    private String workPhone;
    private String workExt;
    private String cellPhone;
    private String email;
    private String checkBoxEmail;
    private String checkBoxTextMessage;

    // Previous Address
    private String previousAddress1;
    private String previousAddress2;
    private String previousCity;
    private String previousState;
    private String previousZip;

    //Audit
    private Date createdOn;
    private Date modifiedOn;
    private String createdBy;
    private String modifiedBy;
    private Boolean isActive;

    private Long patientId;
}
