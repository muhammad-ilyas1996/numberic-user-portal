package com.numbericsuserportal.LlcNorthwest.companies.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Links a portal user to a Corporate Tools company UUID (created via our POST /companies).
 * Used to scope GET /companies to "my formations" for non–super-admin users.
 */
@Entity
@Table(name = "user_formation_company", uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_formation_company", columnNames = {"user_id", "company_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserFormationCompany {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "company_id", nullable = false, length = 64)
    private String companyId;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "linked_at")
    private Date linkedAt;
}
