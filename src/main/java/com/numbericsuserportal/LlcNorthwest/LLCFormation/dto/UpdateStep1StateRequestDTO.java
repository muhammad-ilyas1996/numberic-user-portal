package com.numbericsuserportal.LlcNorthwest.LLCFormation.dto;

import lombok.Data;

@Data
public class UpdateStep1StateRequestDTO {
    private String jurisdiction; // TX
    private Boolean operatesInFormationState; // yes/no
    private String ownershipType; // single|multi

    // required if operatesInFormationState=false
    private String operatingBusinessStreet;
    private String operatingBusinessCity;
    private String operatingBusinessState;
    private String operatingBusinessZip;
}

