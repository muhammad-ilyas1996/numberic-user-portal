package com.medicalbillinguserportal.claim.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Data
public class ClaimFullResponseDto {

    private Long claimId;
    private Long patientId;

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

    private String salesRep;
    private String clinicName;
    private String accession;
    private String ambulanceInfo;
    private String epStdReferral;
    private String paperWork;
    private String specialManipulation;
    private String homeBound;

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

    private Double total;
    private Double insuranceTotal;

    private List<ClaimServiceLineDTO> serviceLines;

    private Double amountCollectedTotal;

    private List<ClaimPaymentDTO> claimPayment;

    private String description;
    private String enteredBy;
    private LocalDate enteredDate;
    private String enteredNotes;
    private LocalDate ledgerDate;

    private Date createdOn;
    private Date modifiedOn;
    private String createdBy;
    private String modifiedBy;
    private Boolean isActive;
}
