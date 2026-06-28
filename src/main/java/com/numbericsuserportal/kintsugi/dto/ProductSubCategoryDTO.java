package com.numbericsuserportal.kintsugi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductSubCategoryDTO {
    private String name;
    private String description;
    private String example;
    private Boolean isFrequent;
}
