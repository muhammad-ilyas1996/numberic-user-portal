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
    private String accountHolderType;

    /** NMI gateway login username. Auto-generated from email when omitted. */
    private String username;
    private String timezone;
    private String language;

    /** Per-merchant fee plan (NMI costPlan). Overrides platform default in application.properties. */
    private String feeScheduleId;
    /** Per-merchant TOS/fees agreement id for complete step. Overrides platform default. */
    private String agreementTextId;
    /** When true, run NMI complete/active step for this merchant. Null = use platform default. */
    private Boolean autoComplete;

    /**
     * Full POST /v4/processors JSON for this merchant. merchantId/gatewayId filled at runtime if omitted.
     * Overrides platform processor template when provided.
     */
    private Map<String, Object> processorPayload;

    /** Optional value-added services for this merchant (each item = one POST /v4/processors body). */
    private java.util.List<Map<String, Object>> vasPayloads;

    /** Exact reseller/NMI boarding payload. Values here override common-field defaults on create merchant. */
    private Map<String, Object> resellerPayload;
}
