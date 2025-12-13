package com.numbericsuserportal.LlcNorthwest.registeredagent.dto;

import lombok.Data;
import java.util.List;

@Data
public class RegisteredAgentProductsResponseDTO {
    private Boolean success;
    private String timestamp;
    private List<RegisteredAgentProductDTO> result;
}

