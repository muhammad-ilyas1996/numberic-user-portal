package com.numbericsuserportal.invoice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceAndTaxRequestDto {
    private Long Id;
    private String  customerName;
    private String invoiceNum;
}
