package com.medicalbillinguserportal.patientregistration.association.entity;

import com.medicalbillinguserportal.commonpersistence.entity.BaseEntity;
import com.medicalbillinguserportal.patientregistration.patientinformation.entity.PatientInfoEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "patient_association")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PatientAssociationEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Referring Provider Information
    private String associationProvider;
    private String associationReferringProvider;
    private String associationPriorAuthorization;
    private String associationOtherReferralSource;

    // PCP Information
    private String patientOutsidePCP;
    private LocalDate lastSeenByPCP;

//New Association
    @OneToMany(mappedBy = "patientAssociation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PatientOtherAssociationEntity> patientOtherAssociation = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_info_id")
    private PatientInfoEntity patientInfoEntity;
}
