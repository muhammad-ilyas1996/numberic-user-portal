package com.medicalbillinguserportal.patientregistration.association.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PatientOtherAssociationDto {

    private Long id;
    // New Association
    private String firstName;
    private String lastName;
    private String role;
    private String email;
    private String phone;
    private String ext;
    private String fax;
}
