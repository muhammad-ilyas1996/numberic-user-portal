package com.medicalbillinguserportal.patientregistration.guarantorinfo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

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
    private String maritalStatus;
    private String employmentStatus;

    private String homePhone;
    private String workPhone;
    private String workPhoneExt;
    private String cellPhone;
    private String email;

    private String previousAddress1;
    private String previousAddress2;
    private String previousCity;
    private String previousState;
    private String previousZip;

    private Long patientId;
}
