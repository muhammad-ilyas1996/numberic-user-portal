package com.numbericsuserportal.invoice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/** Public DTO for pay-by-token page: invoice summary so user can see amount and pay. */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvoicePayByTokenDto {

    private Long invoiceId;
    private String invoiceNum;
    private Double amount;       // total to pay (totalTaxAmountCalculated or taxableAmount)
    private String currency;
    private String customerName;
    private String description;
    private LocalDate dueDate;
    private boolean valid;       // true if token matched and invoice is active and not already PAID
    private boolean paymentEnabled; // false when merchant NMI is not ready for online payment
}
