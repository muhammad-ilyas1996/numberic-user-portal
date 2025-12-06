package com.numbericsuserportal.LlcNorthwest.entity;

import com.numbericsuserportal.commonpersistence.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@Entity
@Table(name = "filing_method")
@Data
@EqualsAndHashCode(callSuper = true)
public class FilingMethodEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "filing_method_id")
    private Long filingMethodId;

    @Column(name = "external_id", unique = true, nullable = false, length = 36)
    private UUID externalId;

    @Column(name = "name")
    private String name;

    @Column(name = "type")
    private String type; // mail, online, or fax

    @Column(name = "cost")
    private String cost;

    @Column(name = "filed_in_days")
    private Integer filedInDays;

    @Column(name = "filed_in_hours")
    private Integer filedInHours;

    @Column(name = "docs_in_days")
    private Integer docsInDays;

    @Column(name = "docs_in_hours")
    private Integer docsInHours;

    @Column(name = "agency_name")
    private String agencyName;

    @Column(name = "filing_description", columnDefinition = "TEXT")
    private String filingDescription;

    @Column(name = "jurisdiction")
    private String jurisdiction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "filing_product_id", nullable = false)
    private FilingProductEntity filingProduct;
}

