package com.numbericsuserportal.taxkintsugi.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "transaction")
@Getter
@Setter
public class TransactionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Instant date;
    private String externalId;
    private String currency;
    private String description;
    private String source;
    private Boolean marketplace;
    private Boolean nexusMet;
    private Boolean hasActiveRegistration;
    private Double totalTaxAmountCalculated;
    private Double taxableAmount;
    private Double taxRateCalculated;

    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AddressEntity> addresses;

    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TransactionItemEntity> transactionItems;
}
