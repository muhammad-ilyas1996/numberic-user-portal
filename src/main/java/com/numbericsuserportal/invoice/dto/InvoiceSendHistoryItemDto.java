package com.numbericsuserportal.invoice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceSendHistoryItemDto {

    private Long id;
    private Long invoiceId;
    private String channel;   // WHATSAPP, EMAIL
    private String sentTo;    // phone or email
    private Date sentAt;
    private String messageSid; // Twilio SID when WhatsApp
}
