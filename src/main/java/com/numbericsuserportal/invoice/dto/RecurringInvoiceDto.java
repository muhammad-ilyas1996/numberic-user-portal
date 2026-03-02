package com.numbericsuserportal.invoice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecurringInvoiceDto {

    private Long id;
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

    private String frequency;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate nextRunOn;
    private String status;
    private Integer runCount;
    private Integer dueDays;

    private Date createdOn;
    private Date modifiedOn;
    private String createdBy;
    private String modifiedBy;

    private List<RecurringInvoiceItemDto> items = new ArrayList<>();
}
