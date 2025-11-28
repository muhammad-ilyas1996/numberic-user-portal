package com.medicalbillinguserportal.claim.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "ClaimPayment")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClaimPayment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    private LocalDate paymentDate;
    private Double paymentAmount;
    private String coPay;
    private String selfPay;
    private String outstandingBalance;
    private String nonClaimCharge;
    private Double unappliedAmount;
    private String applyToClaim;
    @ManyToOne
    @JoinColumn(name = "claim_id")
    private ClaimEntity claim;
}
