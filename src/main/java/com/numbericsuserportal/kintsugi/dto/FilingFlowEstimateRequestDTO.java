package com.numbericsuserportal.kintsugi.dto;

import lombok.Data;

@Data
public class FilingFlowEstimateRequestDTO {
    private String category;
    private String subcategory;
    private String stateCode;
    private String postalCode;
    private String city;
    private Double amount;
    private Double quantity;
    private Boolean simulateActiveRegistration;
}
