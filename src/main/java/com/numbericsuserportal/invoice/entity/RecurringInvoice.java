package com.numbericsuserportal.invoice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "recurring_invoice")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecurringInvoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Optional label for UI e.g. "Monthly retainer" */
    private String name;

    /** Template fields (same as InvoiceAndTaxEntity) */
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

    /** Frequency: WEEKLY, MONTHLY, YEARLY */
    @Column(nullable = false, length = 20)
    private String frequency;

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate endDate;

    /** Next run date (when to generate the next invoice) */
    @Column(nullable = false)
    private LocalDate nextRunOn;

    /** ACTIVE, PAUSED, ENDED */
    @Column(nullable = false, length = 20)
    private String status = "ACTIVE";

    /** Incremented each time we generate an invoice (for invoice number sequence) */
    private int runCount = 0;

    /** Number of days after issue that invoice is due (e.g. 30) */
    private Integer dueDays = 30;

    @Column(name = "created_by", length = 64)
    private String createdBy;
    @Column(name = "created_on")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdOn;
    @Column(name = "modified_by", length = 64)
    private String modifiedBy;
    @Column(name = "modified_on")
    @Temporal(TemporalType.TIMESTAMP)
    private Date modifiedOn;

    @OneToMany(mappedBy = "recurringInvoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecurringInvoiceItem> items = new ArrayList<>();

    public enum Frequency { WEEKLY, MONTHLY, YEARLY }
    public enum Status { ACTIVE, PAUSED, ENDED }
}
