package com.numbericsuserportal.LlcNorthwest.LLCFormation.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class StateCatalogSeedItemDTO {
    private String state;

    @JsonProperty("abbreviation")
    @JsonAlias("abbr")
    private String abbreviation;

    @JsonProperty("filingFeeCents")
    @JsonAlias("filing_fee_cents")
    private Integer filingFeeCents;

    @JsonProperty("annualReportFee")
    @JsonAlias("annual_report_fee")
    private String annualReportFee;

    @JsonProperty("processingTime")
    @JsonAlias("processing_time")
    private String processingTime;

    private String speed;

    private String notes;

    @JsonProperty("nwServiceFeeCents")
    @JsonAlias("nw_service_fee_cents")
    private Integer nwServiceFeeCents;

    @JsonProperty("nwRaYear1Cents")
    @JsonAlias("nw_ra_year1_cents")
    private Integer nwRaYear1Cents;

    @JsonProperty("nwRaRenewalCents")
    @JsonAlias("nw_ra_renewal_cents")
    private Integer nwRaRenewalCents;

    private Boolean active;
}
