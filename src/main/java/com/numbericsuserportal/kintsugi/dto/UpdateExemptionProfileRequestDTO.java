package com.numbericsuserportal.kintsugi.dto;

import lombok.Data;

import java.util.List;

@Data
public class UpdateExemptionProfileRequestDTO {
    private String stateCode;
    private List<String> taxableCategories;
    private List<String> exemptCategories;
}
