package com.numbericsuserportal.LlcNorthwest.LLCFormation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StateCatalogMetaDTO {
    private Integer numbricsServiceFeeCents;
    private Integer nwRaYear1PassThroughCents;
    private Integer nwRaRenewalCents;
    private Integer nwServiceFeeCents;
    private String source;
    private int stateCount;
}
