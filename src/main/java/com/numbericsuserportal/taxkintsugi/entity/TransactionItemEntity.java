package com.numbericsuserportal.taxkintsugi.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "transaction_item")
@Getter
@Setter
public class TransactionItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String externalId;
    private Instant date;
    private String description;
    private String externalProductId;
    private String productName;
    private String productDescription;
    private String productSource;
    private String productSubcategory;
    private String productCategory;
    private Double quantity;
    private Double amount;
    private Boolean exempt;
    private Double taxAmount;
    private Double taxableAmount;
    private Double taxRate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id")
    private TransactionEntity transaction;

    @OneToMany(mappedBy = "transactionItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TaxItemEntity> taxItems;
}