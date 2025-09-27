package com.medicalbillinguserportal.patientregistration.patientinformation.dto;

import lombok.Data;

@Data
public class PatientInfoIdDto {
    private Long id;
    private String cellPhone;
    private String email;
    private String patientFirstName;
    private String patientAddress1;
    private String patientMiddleName;
    private String patientLastName;
}
