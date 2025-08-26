package com.medicalbillinguserportal.patientregistration.message.entity;

import com.medicalbillinguserportal.commonpersistence.entity.BaseEntity;
import com.medicalbillinguserportal.patientregistration.patientinformation.entity.PatientInfoEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "patient_message")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PatientMessageEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 500, nullable = false)
    private String note;

    @Column(length = 50)
    private String type; // e.g., Clinical, Administrative, etc.

    @Column(name = "alert_on_scheduling")
    private Boolean alertOnScheduling = false;

    @Column(name = "alert_on_billing")
    private Boolean alertOnBilling = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_info_id")
    private PatientInfoEntity patientInfoEntity;
}
