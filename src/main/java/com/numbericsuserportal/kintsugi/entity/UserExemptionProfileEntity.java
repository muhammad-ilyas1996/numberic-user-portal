package com.numbericsuserportal.kintsugi.entity;

import com.numbericsuserportal.kintsugi.domain.SalesTaxBusinessType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_exemption_profiles", uniqueConstraints = {
        @UniqueConstraint(name = "uq_user_state_biz", columnNames = {"user_id", "state_code", "business_type"})
})
@Getter
@Setter
public class UserExemptionProfileEntity {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "business_type", nullable = false, length = 30)
    private SalesTaxBusinessType businessType;

    @Column(name = "state_code", nullable = false, length = 2)
    private String stateCode;

    @Column(name = "taxable_categories", columnDefinition = "JSON")
    private String taxableCategoriesJson;

    @Column(name = "exempt_categories", columnDefinition = "JSON")
    private String exemptCategoriesJson;

    @Column(name = "taalr_suggested")
    private Boolean taalrSuggested = true;

    @Column(name = "user_modified")
    private Boolean userModified = false;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        if (lastUpdated == null) {
            lastUpdated = LocalDateTime.now();
        }
    }

    @PreUpdate
    void preUpdate() {
        lastUpdated = LocalDateTime.now();
    }
}
