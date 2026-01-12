package com.numbericsuserportal.registration.entity;

import com.numbericsuserportal.commonpersistence.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "business_profiles")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class BusinessProfile extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "business_profile_id")
    private Long businessProfileId;
    
    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;
    
    @Column(name = "company_name", length = 255)
    private String companyName; // From step 1
    
    @Column(name = "business_legal_name", length = 255)
    private String businessLegalName;
    
    @Column(name = "dba", length = 255)
    private String dba;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "business_type", length = 50)
    private BusinessType businessType;
    
    @Column(name = "industry", length = 100)
    private String industry;
    
    @Column(name = "naics_code", length = 20)
    private String naicsCode;
    
    @Column(name = "ein", length = 20)
    private String ein;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "tax_classification", length = 50)
    private TaxClassification taxClassification;
    
    @Column(name = "website", length = 255)
    private String website;
    
    @Column(name = "business_address_id")
    private Long businessAddressId;
    
    @Column(name = "mailing_address_id")
    private Long mailingAddressId;
    
    @Column(name = "mailing_address_same")
    private Boolean mailingAddressSame = true;
    
    public enum BusinessType {
        llc, corp, sole_proprietor, partnership, nonprofit, other
    }
    
    public enum TaxClassification {
        disregarded_single_member, partnership, c_corp, s_corp, nonprofit
    }
}

