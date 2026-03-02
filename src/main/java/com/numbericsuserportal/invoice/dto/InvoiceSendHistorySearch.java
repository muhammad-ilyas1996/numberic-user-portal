package com.numbericsuserportal.invoice.dto;

import lombok.Data;

@Data
public class InvoiceSendHistorySearch {

    /** Filter by invoice id (optional; if null, return history across all invoices for current user later) */
    private Long invoiceId;
    private Integer pageNumber = 1;
    private Integer pageSize = 20;
}
