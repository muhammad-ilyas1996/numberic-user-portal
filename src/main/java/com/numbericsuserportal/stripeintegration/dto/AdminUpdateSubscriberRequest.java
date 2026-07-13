package com.numbericsuserportal.stripeintegration.dto;

import lombok.Data;

@Data
public class AdminUpdateSubscriberRequest {
    private String plan;
    private Boolean hybridAddOn;
    private Integer seats;
    private String subscriptionStatus; // TRIAL, ACTIVE, CANCELLED, PAYMENT_FAILED, INACTIVE
    private Integer extendTrialDays;
    private Boolean paymentCompleted;
    private Long subscriptionAmountCents; // optional override
}
