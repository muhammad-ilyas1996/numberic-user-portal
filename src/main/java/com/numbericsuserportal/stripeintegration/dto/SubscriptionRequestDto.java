package com.numbericsuserportal.stripeintegration.dto;

import lombok.Data;

@Data
public class SubscriptionRequestDto {
    private String email;       // user email
    private String paymentMethodId; // frontend se aayega (SetupIntent)
    private String priceId;
}
