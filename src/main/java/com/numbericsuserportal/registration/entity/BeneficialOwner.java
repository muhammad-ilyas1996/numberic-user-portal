package com.numbericsuserportal.registration.entity;

import com.numbericsuserportal.commonpersistence.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "beneficial_owners")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class BeneficialOwner extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "owner_id")
    private Long ownerId;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "full_name", nullable = false, length = 200)
    private String fullName;
    
    @Column(name = "title", length = 100)
    private String title;
    
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;
    
    @Column(name = "ssn_last4", length = 4)
    private String ssnLast4;
    
    @Column(name = "ssn_encrypted", columnDefinition = "TEXT")
    private String ssnEncrypted; // Encrypted full SSN if needed
    
    @Column(name = "owner_address_id")
    private Long ownerAddressId;
    
    @Column(name = "phone", length = 20)
    private String phone;
    
    @Column(name = "email", length = 128)
    private String email;
    
    @Column(name = "ownership_percent")
    private Double ownershipPercent;
    
    @Column(name = "ssn_consent")
    private Boolean ssnConsent = false;
}

