package com.numbericsuserportal.invoice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SendInvoiceResponseDto {

    private boolean success;
    private String message;
    private Long sendLogId; // id of invoice_send_log row
}
