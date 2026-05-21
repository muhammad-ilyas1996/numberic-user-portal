package com.numbericsuserportal.invoice.dto;

import lombok.Data;

import java.util.Map;

/**
 * Merchant boarding request collected inside Numbrics.
 *
 * The common fields support the app UI. resellerPayload can carry the exact NMI/reseller
 * boarding schema once provided by the reseller without requiring another backend release.
 */
@Data
public class NmiMerchantOnboardingRequestDto {

    private String businessName;
    private String legalName;
    private String dbaName;
    private String ein;
    private String businessType;
    private String mcc;
    private String website;

    private String contactFirstName;
    private String contactLastName;
    private String contactEmail;
    private String contactPhone;

    private String address1;
    private String address2;
    private String city;
    private String state;
    private String zip;
    private String country;

    private String ownerFirstName;
    private String ownerLastName;
    private String ownerEmail;
    private String ownerPhone;
    private String ownerSsnLast4;
    private Double ownershipPercent;

    private Double monthlyVolume;
    private Double averageTicket;
    private Double highTicket;

    private String settlementBankName;
    private String settlementRoutingNumber;
    private String settlementAccountNumber;
    private String settlementAccountType;

    /** Exact reseller/NMI boarding payload. Values here override common-field defaults. */
    private Map<String, Object> resellerPayload;
}
