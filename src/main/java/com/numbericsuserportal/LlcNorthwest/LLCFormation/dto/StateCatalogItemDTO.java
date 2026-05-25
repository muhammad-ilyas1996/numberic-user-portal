package com.numbericsuserportal.LlcNorthwest.LLCFormation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StateCatalogItemDTO {
    private String state;
    private String abbreviation;
    private Integer filingFeeCents;
    private Integer nwServiceFeeCents;
    private Integer nwRaYear1Cents;
    private Integer nwRaRenewalCents;
    private Integer year1NwTotalCents;
    private Integer year1NumbricsEstimateCents;
    private String annualReportFee;
    private String processingTime;
    private String speed;
    private String notes;
}
