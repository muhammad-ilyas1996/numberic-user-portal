package com.numbericsuserportal.stripeintegration.common;

public enum SubscriptionPlan {

    BASIC(5000L, "Basic Plan - $50"),      // $50 in cents
    PREMIUM(9000L, "Premium Plan - $90");   // $90 in cents

    private final Long amount;
    private final String description;

    SubscriptionPlan(Long amount, String description) {
        this.amount = amount;
        this.description = description;
    }

    public Long getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }

    public Double getAmountInDollars() {
        return amount / 100.0;
    }
}
