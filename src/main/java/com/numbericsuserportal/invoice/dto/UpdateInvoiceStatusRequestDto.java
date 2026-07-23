package com.numbericsuserportal.invoice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateInvoiceStatusRequestDto {
    /** Invoice primary key (required). */
    private Long id;
    /** New status, e.g. DRAFT, SENT, UNPAID, PAID, OVERDUE, CANCELLED, VOID. */
    private String invoiceStatus;
}
