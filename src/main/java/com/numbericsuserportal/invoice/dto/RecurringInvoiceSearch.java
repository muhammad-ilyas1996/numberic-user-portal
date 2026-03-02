package com.numbericsuserportal.invoice.dto;

import lombok.Data;

@Data
public class RecurringInvoiceSearch {
    private Integer pageNumber = 1;
    private Integer pageSize = 20;
    /** Filter by status: ACTIVE, PAUSED, ENDED (optional) */
    private String status;
}
