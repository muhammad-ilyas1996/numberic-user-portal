package com.numbericsuserportal.invoice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Logs each time an invoice is sent (e.g. via WhatsApp or email).
 * Used for send history and for payment link token (token stored here for validation when user opens link).
 */
@Entity
@Table(name = "invoice_send_log")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceSendLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "invoice_id", nullable = false)
    private Long invoiceId;

    @Column(name = "channel", nullable = false, length = 20)
    private String channel; // WHATSAPP, EMAIL

    @Column(name = "sent_to", nullable = false, length = 255)
    private String sentTo; // phone number (E.164) or email

    @Column(name = "sent_at", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date sentAt;

    @Column(name = "token", length = 64)
    private String token; // for payment link ?token=xxx

    @Column(name = "message_sid", length = 50)
    private String messageSid; // Twilio message SID when sent via WhatsApp

    @Column(name = "created_by", length = 64)
    private String createdBy;

    @Column(name = "created_on")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdOn;
}
