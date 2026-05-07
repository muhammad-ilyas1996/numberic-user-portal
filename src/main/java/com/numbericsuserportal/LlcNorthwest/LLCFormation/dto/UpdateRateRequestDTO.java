package com.numbericsuserportal.LlcNorthwest.LLCFormation.dto;

import lombok.Data;

@Data
public class UpdateRateRequestDTO {
    private String rateType;   // NUMBRICS_FEE, EIN_FEE, SCORP_FEE, STATE_FEE, SPEED_FEE
    private String stateCode;  // optional, required for STATE_FEE/SPEED_FEE
    private String speedCode;  // optional, required for SPEED_FEE
    private Integer amountCents;
    private Boolean active;
}

