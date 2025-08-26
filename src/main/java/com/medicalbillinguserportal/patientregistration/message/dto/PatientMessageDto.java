package com.medicalbillinguserportal.patientregistration.message.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PatientMessageDto {
    private Long id;
    private String note;
    private String type;
    private Boolean alertOnScheduling;
    private Boolean alertOnBilling;
    private LocalDateTime createdAt;
    private Long patientId;
}
