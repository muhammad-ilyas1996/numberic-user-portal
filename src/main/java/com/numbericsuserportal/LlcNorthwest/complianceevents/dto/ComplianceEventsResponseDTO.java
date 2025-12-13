package com.numbericsuserportal.LlcNorthwest.complianceevents.dto;

import lombok.Data;
import java.util.List;

@Data
public class ComplianceEventsResponseDTO {
    private Boolean success;
    private String timestamp;
    private List<ComplianceEventDTO> result;
}

