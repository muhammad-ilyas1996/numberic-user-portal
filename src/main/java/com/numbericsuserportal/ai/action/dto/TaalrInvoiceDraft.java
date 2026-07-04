package com.numbericsuserportal.ai.action.dto;

import lombok.Data;

@Data
public class TaalrInvoiceDraft {

    private Long invoiceId;
    private String invoiceNum;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private Double amount;
    private String description;
    /** WHATSAPP or EMAIL */
    private String channel;
    private String recipientPhoneOrEmail;
}
