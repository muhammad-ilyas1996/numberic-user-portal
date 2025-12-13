package com.numbericsuserportal.LlcNorthwest.registeredagent.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class RegisteredAgentProductDTO {
    private UUID id;
    private String price;
    private String jurisdiction;
    private Integer duration;
    private String name;
}

