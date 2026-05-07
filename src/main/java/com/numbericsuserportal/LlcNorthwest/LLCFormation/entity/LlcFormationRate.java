package com.numbericsuserportal.LlcNorthwest.LLCFormation.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "llc_formation_rate", uniqueConstraints = {
        @UniqueConstraint(name = "uk_llc_formation_rate_type_state_speed",
                columnNames = {"rate_type", "state_code", "speed_code"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LlcFormationRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rate_type", nullable = false, length = 32)
    private String rateType; // NUMBRICS_FEE, EIN_FEE, SCORP_FEE, STATE_FEE, SPEED_FEE

    @Column(name = "state_code", length = 10)
    private String stateCode; // for STATE_FEE/SPEED_FEE

    @Column(name = "speed_code", length = 16)
    private String speedCode; // standard|expedited|sameday (for SPEED_FEE)

    @Column(name = "amount_cents", nullable = false)
    private Integer amountCents;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    void prePersist() {
        OffsetDateTime now = OffsetDateTime.now();
        if (createdAt == null) createdAt = now;
        updatedAt = now;
        if (active == null) active = true;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}

