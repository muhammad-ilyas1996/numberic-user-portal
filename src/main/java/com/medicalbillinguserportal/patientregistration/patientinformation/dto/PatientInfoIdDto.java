package com.medicalbillinguserportal.patientregistration.patientinformation.dto;

import lombok.Data;

@Data
public class PatientInfoIdDto {
    private Long id;
    private String cellPhone;
    private String email;
    private String firstName;
    private String address1;
    private String middleName;
    private String lastName;
}
