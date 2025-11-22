package com.numbericsuserportal.taxkintsugi.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tax_item")
@Getter
@Setter
public class TaxItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private Double rate;
    private Double amount;
    private Boolean exempt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_item_id")
    private TransactionItemEntity transactionItem;
}