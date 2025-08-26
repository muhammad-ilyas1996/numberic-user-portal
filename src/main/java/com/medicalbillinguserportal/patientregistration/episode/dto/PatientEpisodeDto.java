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
    private LocalDate startDate;
    private LocalDate endDate;
    private String diagnosis;
    private String status;
    private String description;

    private Long patientId;
}
