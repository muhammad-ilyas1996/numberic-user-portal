package com.numbericsuserportal.invoice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecurringInvoiceItemDto {
    private Long id;
    private String productName;
    private Double quantity;
    private Double amount;
    private String description;
}
