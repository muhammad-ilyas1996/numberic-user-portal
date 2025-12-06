package com.numbericsuserportal.LlcNorthwest.entity;

import com.numbericsuserportal.commonpersistence.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "filing_product")
@Data
@EqualsAndHashCode(callSuper = true)
public class FilingProductEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "filing_product_id")
    private Long filingProductId;

    @Column(name = "external_id", unique = true, nullable = false, length = 36)
    private UUID externalId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "filing_name")
    private String filingName;

    @Column(name = "price")
    private String price;

    @OneToMany(mappedBy = "filingProduct", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FilingMethodEntity> filingMethods;
}

