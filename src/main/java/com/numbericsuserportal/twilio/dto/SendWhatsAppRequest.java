package com.numbericsuserportal.twilio.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for sending WhatsApp messages
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SendWhatsAppRequest {
    /**
     * Recipient WhatsApp number in E.164 format (e.g., +1234567890)
     */
    private String to;
    
    /**
     * WhatsApp message content
     */
    private String message;
}

