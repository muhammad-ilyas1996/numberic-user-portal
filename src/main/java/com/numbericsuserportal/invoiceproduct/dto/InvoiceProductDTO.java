package com.numbericsuserportal.invoiceproduct.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceProductDTO {

    private Long id;
    private String productName;
    private Double quantity;
    private Double amount;
    private String description;
}
