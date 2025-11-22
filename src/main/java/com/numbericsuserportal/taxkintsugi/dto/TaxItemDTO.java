package com.numbericsuserportal.taxkintsugi.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaxItemDTO {
    private String name;
    private Double rate;
    private Double amount;
    private Boolean exempt;
}