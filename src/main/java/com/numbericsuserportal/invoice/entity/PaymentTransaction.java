package com.numbericsuserportal.invoice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Records each payment (e.g. invoice paid via NMI).
 * Used for "Payments & transactions" list.
 */
@Entity
@Table(name = "payment_transaction")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "invoice_id", nullable = false)
    private Long invoiceId;

    @Column(name = "invoice_num", length = 50)
    private String invoiceNum;   // for display in list

    @Column(nullable = false)
    private Double amount;

    @Column(length = 10)
    private String currency = "USD";

    @Column(name = "gateway", nullable = false, length = 50)
    private String gateway; // NMI, STRIPE, etc.

    @Column(name = "gateway_transaction_id", length = 100)
    private String gatewayTransactionId;

    @Column(name = "auth_code", length = 50)
    private String authCode;

    @Column(nullable = false, length = 20)
    private String status; // SUCCESS, FAILED

    @Column(name = "paid_at", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date paidAt;

    @Column(name = "payer_email", length = 255)
    private String payerEmail;

    @Column(length = 500)
    private String description;

    @Column(name = "created_on")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdOn;
}
