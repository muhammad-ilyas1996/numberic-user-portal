package com.medicalbillinguserportal.claim.entity;

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
@Table(name = "Claim")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClaimEntity extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "claim_id")
    private Long claimId;


    private String claimTransmission;

    private String payerAddress;

    private LocalDate dateOfService;
    private String renderingProvider;
    private String location;
    private String supervisingProvider;
    private String billingProvider;
    private String payToProvider;
    private String referringProvider;
    private String servicingProvider;
    private String feeSchedule;
    private String payToLocation;

    //Lab Info

    private String salesRep;
    private String clinicName;
    private String accession;
    private String ambulanceInfo;
    private String epStdReferral;
    private String paperWork;
    private String specialManipulation;
    private String homeBound;

    //Patient History
    private String phCode1;
    private String phCode2;
    private String phCode3;
    private String phCode4;
    private String phCode5;
    private String phCode6;
    private String phCode7;
    private String phCode8;
    private String phCode9;
    private String phCode10;
    private String phCode11;
    private String phCode12;

    //Service Form
    private Double Total;
    private Double InsuranceTotal;

    @OneToMany(mappedBy = "claim", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ClaimServiceLineEntity> serviceLines = new ArrayList<>();
    //Amount Collected From Patient

    private Double amountCollectedTotal;

    //Post Patient Payment
    @OneToMany(mappedBy = "claim", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ClaimPayment> claimPayment = new ArrayList<>();

    //Claim Notes
    private String description;
    private String enteredBy;
    private LocalDate enteredDate;
    private String enteredNotes;

    private LocalDate ledgerDate;

    @ManyToOne
    @JoinColumn(name = "patient_id")
    private PatientInfoEntity patientInfoEntity;
}
