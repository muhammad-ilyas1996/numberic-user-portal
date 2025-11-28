package com.medicalbillinguserportal.claim.dto;

import lombok.Data;

@Data
public class ClaimServiceLineDTO {
    private Long id;
    private String fromDate;
    private String toDate;
    private String pos;
    private String emg;
    private String cptCode;
    private String modifiers;
    private String diagnosisPointer;
    private Double fee;
    private Integer units;
    private String charge;
    private String epStd;
    private String familyPlan;
}
