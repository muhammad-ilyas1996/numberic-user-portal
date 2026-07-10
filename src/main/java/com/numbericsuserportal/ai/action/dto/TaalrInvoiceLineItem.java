package com.numbericsuserportal.ai.action.dto;

import lombok.Data;

@Data
public class TaalrInvoiceLineItem {
    private String name;
    private Double quantity = 1.0;
    private Double amount;
}
