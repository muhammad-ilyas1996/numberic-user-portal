package com.numbericsuserportal.invoice.dto;

import lombok.Data;

@Data
public class InvoiceSearch {

    private Integer pageNumber;
    private Integer pageSize;
    private String fromDate;
    private String toDate;
}
