package com.numbericsuserportal.taxkintsugi.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tax_address")
@Getter
@Setter
public class AddressEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type;
    private String street1;
    private String street2;
    private String city;
    private String county;
    private String state;
    private String postalCode;
    private String country;
    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id")
    private TransactionEntity transaction;
}
