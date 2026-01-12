package com.numbericsuserportal.registration.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationStatusDto {
    
    private Long userId;
    private String email;
    private RegistrationStatus status;
    private Integer currentStep;
    private Boolean isCompleted;
    
    public enum RegistrationStatus {
        STEP1_COMPLETED,
        STEP2_COMPLETED,
        STEP3_COMPLETED,
        STEP4_COMPLETED,
        STEP5_COMPLETED,
        STEP6_COMPLETED,
        STEP7_COMPLETED,
        COMPLETED,
        INCOMPLETE
    }
}

