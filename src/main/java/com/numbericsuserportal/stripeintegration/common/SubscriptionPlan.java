package com.numbericsuserportal.stripeintegration.common;

/**
 * Legacy reference enum. Prefer User.SubscriptionPlan + subscription_plan_catalog table.
 */
public enum SubscriptionPlan {

    SOLOPRENEUR(1900L, "Solopreneur (founder) - $19/mo"),
    BUSINESS_OWNER(7900L, "Business Owner - $79/mo"),
    ACCOUNTANT_PRO(19900L, "Accountant Pro - $199/mo per seat"),
    STARTER(29900L, "Starter Plan - $299/season"),
    PROFESSIONAL(59900L, "Professional Plan - $599/season"),
    ENTERPRISE(129900L, "Enterprise Plan - $1,299/season");

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

    public String getDefaultRoleCode() {
        return switch (this) {
            case SOLOPRENEUR, BUSINESS_OWNER, STARTER -> "NUMBRICS_BUSINESS_OWNER";
            case ACCOUNTANT_PRO, PROFESSIONAL, ENTERPRISE -> "NUMBRICS_ACCOUNTANT_PRO";
        };
    }
}
