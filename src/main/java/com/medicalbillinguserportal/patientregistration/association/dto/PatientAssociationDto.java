package com.medicalbillinguserportal.patientregistration.association.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PatientAssociationDto {
    private Long id;

    private String associationProvider;
    private String associationReferringProvider;
    private String associationPriorAuthorization;
    private String associationOtherReferralSource;

    // PCP Information
    private String patientOutsidePCP;
    private LocalDate lastSeenByPCP;

    // New Association
    private String associationFirstName;
    private String associationLastName;
    private String associationRole;
    private String associationEmail;
    private String associationPhone;
    private String associationExt;
    private String associationFax;

    private Long patientId;
}
