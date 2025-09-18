package com.medicalbillinguserportal.patientregistration.episode.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PatientEpisodeDto {
    private Long id;
    private String title;
    private String defaultEpisode;
    private String employement;
    private String autoAccident;
    private String state;
    private String otherAccident;


    private LocalDate dateOfCurrentIllness;
    private LocalDate dateDisAbilityBegin;
    private LocalDate dateDisAbilityEnd;
    private LocalDate dateHospitalAdmission;
    private LocalDate dateHospitalDischanrge;
    private LocalDate dateAssumedCare;
    private LocalDate dateRelinquishedCare;

    private String accidentType;
    private LocalDate accidentDate;
    private String additionalInfoCombo;
    private String additionalInfo;
    private String specialProgramCode;
    private String claimDelayReason;
    private String claimNotes;
    private String homeBound;

    private Long patientId;
}
