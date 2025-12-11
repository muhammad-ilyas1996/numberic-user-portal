package com.numbericsuserportal.LlcNorthwest.companies.entity;

import com.numbericsuserportal.commonpersistence.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "company_jurisdiction")
@Data
@EqualsAndHashCode(callSuper = true)
public class CompanyJurisdictionEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "company_jurisdiction_id")
    private Long companyJurisdictionId;

    @Column(name = "jurisdiction_name", nullable = false)
    private String jurisdictionName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private CompanyEntity company;
}

