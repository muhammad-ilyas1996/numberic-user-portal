package com.numbericsuserportal.invoice.dto;

import lombok.Data;

import java.util.Date;

@Data
public class NmiMerchantOnboardingStatusDto {

    private Long id;
    private Long userId;
    private String status;
    private String businessName;
    private String legalName;
    private String contactEmail;
    private String nmiApplicationId;
    private String nmiMerchantId;
    private String declinedReason;
    private Boolean credentialsConfigured;
    private String message;
    private Date createdOn;
    private Date updatedOn;
    private Date submittedOn;
    private Date approvedOn;
}
