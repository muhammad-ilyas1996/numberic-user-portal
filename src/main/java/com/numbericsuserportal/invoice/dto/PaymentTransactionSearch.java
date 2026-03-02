package com.numbericsuserportal.invoice.dto;

import lombok.Data;

@Data
public class PaymentTransactionSearch {

    private Integer pageNumber = 1;
    private Integer pageSize = 20;
    private Long invoiceId;      // filter by invoice
    private String status;       // SUCCESS, FAILED (optional)
    private String fromDate;    // filter paidAt >= fromDate
    private String toDate;      // filter paidAt <= toDate
}
