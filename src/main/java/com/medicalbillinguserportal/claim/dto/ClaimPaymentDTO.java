package com.medicalbillinguserportal.claim.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ClaimPaymentDTO {
    private Long paymentId;
    private LocalDate paymentDate;
    private Double paymentAmount;
    private String coPay;
    private String selfPay;
    private String outstandingBalance;
    private String nonClaimCharge;
    private Double unappliedAmount;
    private String applyToClaim;
}
