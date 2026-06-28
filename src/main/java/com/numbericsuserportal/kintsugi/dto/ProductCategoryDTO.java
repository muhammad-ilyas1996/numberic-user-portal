package com.numbericsuserportal.kintsugi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductCategoryDTO {
    private String name;
    private List<ProductSubCategoryDTO> subcategories;
}
