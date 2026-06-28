package com.numbericsuserportal.kintsugi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FilingPaymentIntentResponseDTO {
    private String clientSecret;
    private String paymentIntentId;
    private Long totalCents;
    private String filingId;
}
