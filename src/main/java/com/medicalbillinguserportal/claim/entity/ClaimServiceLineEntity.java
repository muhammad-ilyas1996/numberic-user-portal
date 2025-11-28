package com.medicalbillinguserportal.claim.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ClaimServiceLineEntity")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClaimServiceLineEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fromDate;
    private String toDate;
    private String pos;
    private String emg;
    private String cptCode;
    private String modifiers;
    private String diagnosisPointer;
    private Double fee;
    private Integer units;
    private String charge;
    private String epStd;
    private String FamilyPlan;
    @ManyToOne
    @JoinColumn(name = "claim_id")
    private ClaimEntity claim;
}
