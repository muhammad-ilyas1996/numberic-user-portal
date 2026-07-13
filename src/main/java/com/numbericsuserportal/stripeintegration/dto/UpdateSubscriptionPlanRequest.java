package com.numbericsuserportal.stripeintegration.dto;

import lombok.Data;

@Data
public class UpdateSubscriptionPlanRequest {
    private String displayName;
    private String description;
    private String audience;
    private Long amountCents;
    private Double amount; // dollars convenience
    private Long hybridAddonCents;
    private Double hybridAddonAmount;
    private Integer trialDays;
    private String defaultRoleCode;
    private Boolean perSeat;
    private Integer sortOrder;
    private Boolean active;
    private String featuresJson;
}
