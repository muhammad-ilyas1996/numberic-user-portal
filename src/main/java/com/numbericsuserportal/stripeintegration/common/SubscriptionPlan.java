package com.numbericsuserportal.stripeintegration.common;

/**
 * Subscription Plan Enum
 * Note: This enum is kept for reference. The actual subscription plans are defined in User.SubscriptionPlan
 * and should be used throughout the application.
 * 
 * This enum matches User.SubscriptionPlan for consistency.
 */
public enum SubscriptionPlan {

    STARTER(29900L, "Starter Plan - $299/season"),           // $299
    PROFESSIONAL(59900L, "Professional Plan - $599/season"), // $599
    ENTERPRISE(129900L, "Enterprise Plan - $1,299/season"); // $1,299

    private final Long amount; // Amount in cents
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
    
    /**
     * Helper method to get default role based on plan
     * Note: Use User.SubscriptionPlan.getDefaultRoleCode() in actual implementation
     */
    public String getDefaultRoleCode() {
        return switch (this) {
            case STARTER -> "NUMBRICS_BUSINESS_OWNER";
            case PROFESSIONAL -> "NUMBRICS_ACCOUNTANT_PRO";
            case ENTERPRISE -> "NUMBRICS_ACCOUNTANT_PRO";
        };
    }
}
