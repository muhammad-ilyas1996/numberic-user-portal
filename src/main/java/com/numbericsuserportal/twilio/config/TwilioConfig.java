package com.numbericsuserportal.twilio.config;

import com.twilio.Twilio;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Twilio Configuration
 * Handles Twilio credentials and initialization for WhatsApp messaging
 */
@Configuration
public class TwilioConfig {

    @Value("${twilio.account.sid:}")
    private String accountSid;

    @Value("${twilio.auth.token:}")
    private String authToken;

    @Value("${twilio.api.key:}")
    private String apiKey;

    @Value("${twilio.whatsapp.number:}")
    private String whatsappNumber;

    @PostConstruct
    public void init() {
        if (accountSid == null || accountSid.isEmpty() || 
            authToken == null || authToken.isEmpty()) {
            System.out.println("Warning: Twilio credentials not configured. Twilio WhatsApp service will not work.");
        } else {
            // Initialize Twilio SDK
            Twilio.init(accountSid, authToken);
            System.out.println("Twilio initialized successfully.");
            System.out.println("WhatsApp Number: " + whatsappNumber);
            
            if (apiKey != null && !apiKey.isEmpty()) {
                System.out.println("Twilio API Key configured (for future use).");
            }
        }
    }

    public String getAccountSid() {
        return accountSid;
    }

    public String getAuthToken() {
        return authToken;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getWhatsappNumber() {
        return whatsappNumber;
    }

    public boolean isConfigured() {
        return accountSid != null && !accountSid.isEmpty() && 
               authToken != null && !authToken.isEmpty();
    }
}

