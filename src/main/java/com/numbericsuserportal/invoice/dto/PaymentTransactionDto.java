package com.numbericsuserportal.invoice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentTransactionDto {

    private Long id;
    private Long invoiceId;
    private String invoiceNum;   // from invoice, for display
    private Double amount;
    private String currency;
    private String gateway;
    private String gatewayTransactionId;
    private String authCode;
    private String status;
    private Date paidAt;
    private String payerEmail;
    private String description;
}
