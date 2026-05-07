package com.numbericsuserportal.LlcNorthwest.LLCFormation.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "llc_formation_member")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LlcFormationMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "formation_id", nullable = false)
    private Long formationId;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "dob")
    private LocalDate dob;

    @Column(name = "ssn_last4_enc")
    private String ssnLast4Enc;

    @Column(name = "ownership_pct", nullable = false)
    private Integer ownershipPct;

    @Column(name = "title")
    private String title;

    @Column(name = "is_primary", nullable = false)
    private Boolean primaryMember = false;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    void prePersist() {
        OffsetDateTime now = OffsetDateTime.now();
        if (createdAt == null) createdAt = now;
        updatedAt = now;
        if (primaryMember == null) primaryMember = false;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}

