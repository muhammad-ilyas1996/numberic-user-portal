package com.numbericsuserportal.twilio.dto;

import lombok.Data;

/**
 * DTO for incoming Twilio WhatsApp webhook requests
 * Twilio sends form-encoded data with these parameters
 */
@Data
public class WhatsAppWebhookRequest {
    
    /**
     * The phone number that sent the message (in E.164 format)
     * Example: whatsapp:+1234567890
     */
    private String From;
    
    /**
     * The message body/content
     */
    private String Body;
    
    /**
     * Twilio Message SID (unique identifier for the message)
     */
    private String MessageSid;
    
    /**
     * Optional: Account SID
     */
    private String AccountSid;
    
    /**
     * Optional: To number (your Twilio WhatsApp number)
     */
    private String To;
    
    /**
     * Optional: Number of media items (if any)
     */
    private String NumMedia;

    /**
     * Optional: URL of the first media item (if NumMedia > 0)
     * Twilio sends this as MediaUrl0
     */
    private String MediaUrl0;

    /**
     * Optional: Content type of the first media item (if NumMedia > 0)
     * Twilio sends this as MediaContentType0
     */
    private String MediaContentType0;
}

