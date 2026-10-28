package com.numbericsuserportal.federaltax.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class TaxModuleMenuDto {
    private String code;
    private String label;
    private String phase;
    private boolean available;
    private String notes;
}
