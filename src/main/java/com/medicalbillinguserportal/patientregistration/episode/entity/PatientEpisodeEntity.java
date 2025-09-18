package com.medicalbillinguserportal.patientregistration.episode.entity;

import com.medicalbillinguserportal.commonpersistence.entity.BaseEntity;
import com.medicalbillinguserportal.patientregistration.patientinformation.entity.PatientInfoEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "patient_episode")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PatientEpisodeEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    private String specialProgramCode;
    private String claimDelayReason;
    private String claimNotes;
    private String homeBound;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_info_id")
    private PatientInfoEntity patientInfoEntity;
}
