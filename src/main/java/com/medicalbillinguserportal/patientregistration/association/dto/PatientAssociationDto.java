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

    // Referring Provider Info
    private String referringProvider;
    private String priorAuthorization;
    private String otherReferralSource;

    // PCP Info
    private String defaultPCP;
    private LocalDate lastSeenByPCP;

    // Pharmacy Info
    private String defaultPharmacy;
    private String pharmacyPhoneNumber;

    private Long patientId;
}
