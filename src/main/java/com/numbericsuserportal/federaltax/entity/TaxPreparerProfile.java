package com.numbericsuserportal.federaltax.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "tax_preparer_profiles")
@Data
public class TaxPreparerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "display_name_internal", length = 120)
    private String displayNameInternal;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    /** Soft capacity — lower open-case count preferred. */
    @Column(name = "max_open_cases", nullable = false)
    private int maxOpenCases = 25;

    /** Comma-separated quals e.g. 1040,SCHEDULE_C,COMPLEX */
    @Column(name = "qualifications", length = 255)
    private String qualifications = "FORM_1040,SCHEDULE_C";

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
