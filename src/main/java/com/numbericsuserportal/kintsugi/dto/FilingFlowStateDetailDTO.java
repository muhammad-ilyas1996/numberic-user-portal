package com.numbericsuserportal.kintsugi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FilingFlowStateDetailDTO {
    private String stateCode;
    private String stateName;
    private Map<String, Object> nexus;
    private Map<String, Object> taxEstimate;
}
