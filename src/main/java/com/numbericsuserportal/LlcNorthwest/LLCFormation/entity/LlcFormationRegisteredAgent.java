package com.numbericsuserportal.LlcNorthwest.LLCFormation.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "llc_formation_registered_agent", uniqueConstraints = {
        @UniqueConstraint(name = "uk_llc_formation_registered_agent_formation", columnNames = {"formation_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LlcFormationRegisteredAgent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "formation_id", nullable = false)
    private Long formationId;

    @Column(name = "agent_type", nullable = false, length = 24)
    private String agentType; // NUMBRICS_NW | OWN

    @Column(name = "source", nullable = false, length = 24)
    private String source; // NORTHWEST | USER_PROVIDED

    @Column(name = "northwest_ref_id")
    private String northwestRefId;

    @Column(name = "agent_name_snapshot")
    private String agentNameSnapshot;

    @Column(name = "agent_address_snapshot")
    private String agentAddressSnapshot;

    @Column(name = "agent_name")
    private String agentName;

    @Column(name = "street")
    private String street;

    @Column(name = "city")
    private String city;

    @Column(name = "state", length = 10)
    private String state;

    @Column(name = "zip", length = 10)
    private String zip;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    void prePersist() {
        OffsetDateTime now = OffsetDateTime.now();
        if (createdAt == null) createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}

