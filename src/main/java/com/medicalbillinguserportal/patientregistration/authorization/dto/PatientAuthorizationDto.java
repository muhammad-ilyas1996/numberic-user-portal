package com.medicalbillinguserportal.patientregistration.authorization.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PatientAuthorizationDto {
    private Long id;
    private String servicePerformer;
    private String authorizationNumber;
    private LocalDate authorizationDate;
    private LocalDate expirationDate;
    private LocalDate requestedDate;
    private String referredBy;
    private String payer;
    private String contactPerson;
    private String phoneNo;
    private String authorizationLimitation;
    private String timeRestriction;
    private String timeRestrictionPer;
    private String placeOfService;
    private String status;
    private String notes;
    private Long patientId;
}
