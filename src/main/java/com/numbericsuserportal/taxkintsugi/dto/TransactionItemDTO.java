package com.numbericsuserportal.taxkintsugi.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
public class TransactionItemDTO {
    private String externalId;
    private Instant date;
    private String description;
    private String externalProductId;
    private String productName;
    private String productDescription;
    private String productSource;
    private String productSubcategory;
    private String productCategory;
    private Double quantity;
    private Double amount;
    private Boolean exempt;
    private Double taxAmount;
    private Double taxableAmount;
    private Double taxRate;

    private List<TaxItemDTO> taxItems;
}
