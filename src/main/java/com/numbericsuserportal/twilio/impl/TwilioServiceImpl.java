package com.numbericsuserportal.twilio.impl;

import com.numbericsuserportal.twilio.config.TwilioConfig;
import com.numbericsuserportal.twilio.dto.TwilioResponse;
import com.numbericsuserportal.twilio.service.TwilioService;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Twilio Service Implementation for WhatsApp messaging
 * Handles sending WhatsApp messages via Twilio API
 */
@Service
public class TwilioServiceImpl implements TwilioService {

    @Autowired
    private TwilioConfig twilioConfig;

    @Override
    public TwilioResponse sendWhatsApp(String to, String messageBody) {
        try {
            // Validate configuration
            if (!twilioConfig.isConfigured()) {
                return new TwilioResponse(
                    false, 
                    null, 
                    null, 
                    "Twilio is not configured. Please check application.properties"
                );
            }

            // Validate inputs
            if (to == null || to.trim().isEmpty()) {
                return new TwilioResponse(
                    false, 
                    null, 
                    null, 
                    "Recipient number cannot be empty"
                );
            }

            if (messageBody == null || messageBody.trim().isEmpty()) {
                return new TwilioResponse(
                    false, 
                    null, 
                    null, 
                    "Message content cannot be empty"
                );
            }

            // Format phone number to E.164 format
            String formattedTo = formatPhoneNumber(to);
            String fromNumber = twilioConfig.getWhatsappNumber();

            if (fromNumber == null || fromNumber.isEmpty()) {
                return new TwilioResponse(
                    false, 
                    null, 
                    null, 
                    "WhatsApp number not configured"
                );
            }

            // Send WhatsApp message via Twilio
            Message message = Message.creator(
                    new PhoneNumber("whatsapp:" + formattedTo),
                    new PhoneNumber(fromNumber),
                    messageBody
            ).create();

            return new TwilioResponse(
                true,
                "WhatsApp message sent successfully",
                message.getSid(),
                null
            );

        } catch (Exception e) {
            return new TwilioResponse(
                false,
                null,
                null,
                "Failed to send WhatsApp message: " + e.getMessage()
            );
        }
    }

    /**
     * Format phone number to E.164 format
     * @param phoneNumber Phone number in any format
     * @return Formatted phone number in E.164 format (e.g., +1234567890)
     */
    private String formatPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            throw new IllegalArgumentException("Phone number cannot be empty");
        }

        // Remove all non-digit characters except +
        String cleaned = phoneNumber.replaceAll("[^\\d+]", "");

        // If doesn't start with +, add +1 for US numbers (default)
        if (!cleaned.startsWith("+")) {
            cleaned = "+1" + cleaned;
        }

        return cleaned;
    }
}

