package com.medicalbillinguserportal.patientregistration.association.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

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



    private Long patientId;

    private List<PatientOtherAssociationDto> patientOtherAssociationDto;
}
