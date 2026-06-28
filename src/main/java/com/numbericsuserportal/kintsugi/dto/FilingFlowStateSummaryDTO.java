package com.numbericsuserportal.kintsugi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FilingFlowStateSummaryDTO {
    private String stateCode;
    private String stateName;
    private String nexusStatus;
    private Boolean nexusMet;
    private Boolean economicNexusMet;
    private Boolean physicalNexusMet;
    private String transactionsAmount;
    private String taxLiability;
    private Integer thresholdSales;
    private Boolean hasKintsugiData;
}
