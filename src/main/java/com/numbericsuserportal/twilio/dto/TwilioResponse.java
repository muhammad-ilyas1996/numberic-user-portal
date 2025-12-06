package com.numbericsuserportal.twilio.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for Twilio API operations
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TwilioResponse {
    /**
     * Whether the operation was successful
     */
    private boolean success;
    
    /**
     * Success message
     */
    private String message;
    
    /**
     * Twilio message SID (unique identifier for the message)
     */
    private String messageSid;
    
    /**
     * Error message (if operation failed)
     */
    private String error;
}

