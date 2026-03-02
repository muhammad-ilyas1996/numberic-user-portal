package com.numbericsuserportal.invoice.dto;

import lombok.Data;

@Data
public class SendInvoiceRequestDto {

    private Long invoiceId;
    /** Channel: WHATSAPP or EMAIL (only WHATSAPP implemented for now) */
    private String channel;
    /** For WHATSAPP: recipient phone (E.164 or 10-digit). For EMAIL: recipient email. */
    private String recipientPhoneOrEmail;
}
