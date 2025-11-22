package com.numbericsuserportal.stripeintegration.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SubscriptionResponseDto {
    private String subscriptionId;
    private String status;
}
