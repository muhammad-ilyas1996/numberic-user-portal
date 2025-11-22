package com.numbericsuserportal.invoice.entity;

import com.numbericsuserportal.commonpersistence.entity.BaseEntity;
import com.numbericsuserportal.invoiceproduct.entity.InvoiceProductEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "invoice_tax")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceAndTaxEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double totalTaxAmountCalculated;
    private Double taxableAmount;
    private String  nexusMet;
    private Double taxRateCalculated;
    private String  hasActiveRegistration;
    private String transactionItems;


    private LocalDate invoiceDate;
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

    private String invoiceNum;
    private LocalDate invoiceIssueDate;
    private LocalDate invoiceDueDate;
    private String invoiceStatus;

    @OneToMany(mappedBy = "invoiceAndTaxEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InvoiceProductEntity> invoiceProductEntity = new ArrayList<>();
}


