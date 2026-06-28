package com.numbericsuserportal.kintsugi.dto;

import lombok.Data;

import java.util.List;

@Data
public class EnterSalesEstimateRequestDTO {
    private String stateCode;
    private String category;
    private String subcategory;
    private Double taxableAmount;
    private Double exemptAmount;
    private List<String> exemptCategories;
    private String postalCode;
    private String city;
    /** Optional — links estimate to an existing draft row in sales_tax_filings */
    private String filingId;
}
