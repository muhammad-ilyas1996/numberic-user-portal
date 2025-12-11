package com.numbericsuserportal.LlcNorthwest.companies.entity;

import com.numbericsuserportal.commonpersistence.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "company")
@Data
@EqualsAndHashCode(callSuper = true)
public class CompanyEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "company_id")
    private Long companyId;

    @Column(name = "external_id", unique = true, nullable = false, length = 36)
    private UUID externalId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "entity_type")
    private String entityType;

    @Column(name = "home_state")
    private String homeState;

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CompanyJurisdictionEntity> jurisdictions = new ArrayList<>();
}

