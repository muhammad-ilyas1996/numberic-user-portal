package com.numbericsuserportal.twilio.service;

import com.numbericsuserportal.twilio.dto.TwilioResponse;

/**
 * Twilio Service Interface for WhatsApp messaging
 */
public interface TwilioService {
    /**
     * Send WhatsApp message
     * @param to WhatsApp number in E.164 format (e.g., +1234567890)
     * @param messageBody WhatsApp message content
     * @return TwilioResponse with success status and message SID
     */
    TwilioResponse sendWhatsApp(String to, String messageBody);
}

