package com.numbericsuserportal.ai.action.dto;

import lombok.Data;

@Data
public class TaalrSalesTaxDraft {

    private String filingId;
    private String businessType;
    private String businessTypeDescription;
    private String stateCode;
    private Boolean exemptionsConfirmed;
    private String category;
    private String subcategory;
    private Double taxableAmount;
    private Double exemptAmount;
    /** Ship-to / nexus city for rate estimate (optional; skip uses state default). */
    private String city;
    private String postalCode;
    private Boolean cityCollected;
    private Boolean postalCollected;
    private Boolean registrationRequired;
    private Boolean registrationAcknowledged;
    private String stateTaxAccountId;
    private boolean awaitingReviewConfirm;
}
