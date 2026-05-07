package com.numbericsuserportal.LlcNorthwest.LLCFormation.dto;

import lombok.Data;

@Data
public class UpdateStep4RegisteredAgentRequestDTO {
    private String agentType; // NUMBRICS_NW | OWN

    // Flow A
    private String northwestRefId;
    private String agentNameSnapshot;
    private String agentAddressSnapshot;

    // Flow B
    private String agentName;
    private String street;
    private String city;
    private String state;
    private String zip;
}

