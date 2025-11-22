package com.numbericsuserportal.invoice.dto;

import com.numbericsuserportal.invoiceproduct.dto.InvoiceProductDTO;
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
public class InvoiceAndTaxDTO {

    private Long id;

    private Double totalTaxAmountCalculated;
    private Double taxableAmount;
    private String nexusMet;
    private Double taxRateCalculated;
    private String hasActiveRegistration;
    private String transactionItems;

    private LocalDate invoiceDate;
    private String externalId;
    private String currency;
    private String description;

    // Customer Info
    private String customerName;
    private String customerEmail;
    private String customerStreet;
    private String customerCity;
    private String customerState;
    private String customerPostalCode;
    private String customerCountry;

    // Shipping Info
    private String shipStreet;
    private String shipCity;
    private String shipState;
    private String shipPostalCode;
    private String shipCountry;

    // Invoice Info
    private String invoiceNum;
    private LocalDate invoiceIssueDate;
    private LocalDate invoiceDueDate;
    private String invoiceStatus;

    //Base Entity
    private Date createdOn;
    private Date modifiedOn;
    private String createdBy;
    private String modifiedBy;
    private Boolean isActive;

    // Product List
    private List<InvoiceProductDTO> invoiceProductList = new ArrayList<>();;
}
