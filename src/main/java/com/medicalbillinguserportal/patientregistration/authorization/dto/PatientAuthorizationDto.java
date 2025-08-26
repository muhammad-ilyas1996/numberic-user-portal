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

    private String authorizationNumber;

    private String status;

    private LocalDate requestedDate;

    private String currentPayer;

    private Integer authorizedQty;

    private BigDecimal authAmountSum;

    private LocalDate effectiveDate;

    private LocalDate expirationDate;

    private String approvalType;

    private Boolean test;

    private LocalDate date;

    private String claimNumber;

    private String chapter;

    private Long patientId;
}
