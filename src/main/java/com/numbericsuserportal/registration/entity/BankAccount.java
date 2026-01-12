package com.numbericsuserportal.registration.entity;

import com.numbericsuserportal.commonpersistence.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "bank_accounts")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class BankAccount extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bank_account_id")
    private Long bankAccountId;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "account_holder_name", nullable = false, length = 200)
    private String accountHolderName;
    
    @Column(name = "routing_number", nullable = false, length = 9)
    private String routingNumber;
    
    @Column(name = "account_number_encrypted", columnDefinition = "TEXT")
    private String accountNumberEncrypted; // Encrypted account number
    
    @Column(name = "account_number_last4", length = 4)
    private String accountNumberLast4;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false, length = 20)
    private AccountType accountType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "verification_method", length = 20)
    private VerificationMethod verificationMethod;
    
    @Column(name = "is_verified")
    private Boolean isVerified = false;
    
    public enum AccountType {
        checking, savings
    }
    
    public enum VerificationMethod {
        instant, microdeposits
    }
}

