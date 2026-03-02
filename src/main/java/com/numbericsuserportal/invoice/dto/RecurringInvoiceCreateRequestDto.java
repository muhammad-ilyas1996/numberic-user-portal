package com.numbericsuserportal.invoice.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Request to create a recurring invoice profile (template + schedule). */
@Data
public class RecurringInvoiceCreateRequestDto {

    private String name;

    private Double totalTaxAmountCalculated;
    private Double taxableAmount;
    private String nexusMet;
    private Double taxRateCalculated;
    private String hasActiveRegistration;
    private String transactionItems;

    private String externalId;
    private String currency;
    private String description;

    private String customerName;
    private String customerEmail;
    private String customerStreet;
    private String customerCity;
    private String customerState;
    private String customerPostalCode;
    private String customerCountry;

    private String shipStreet;
    private String shipCity;
    private String shipState;
    private String shipPostalCode;
    private String shipCountry;

    /** WEEKLY, MONTHLY, YEARLY */
    private String frequency;
    private LocalDate startDate;
    private LocalDate endDate;
    /** Days after issue that payment is due (default 30) */
    private Integer dueDays = 30;

    private List<RecurringInvoiceItemDto> items = new ArrayList<>();
}
