package com.numbericsuserportal.twilio.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity for storing tax cases created from WhatsApp messages
 * One active TaxCase per phone number
 */
@Entity
@Table(name = "tax_cases")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaxCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * Foreign key to users table (nullable - case can exist without a user)
     */
    @Column(name = "user_id", nullable = true)
    private Long userId;

    /**
     * Phone number associated with this tax case
     */
    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    /**
     * Status of the tax case
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TaxCaseStatus status;

    /**
     * Timestamp when the case was created
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = TaxCaseStatus.NEW;
        }
    }

    /**
     * Tax Case Status Enum
     */
    public enum TaxCaseStatus {
        NEW,
        NEEDS_DOCUMENTS,
        IN_PROGRESS
    }
}

