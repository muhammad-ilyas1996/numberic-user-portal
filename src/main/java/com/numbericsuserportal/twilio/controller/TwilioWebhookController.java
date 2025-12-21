package com.numbericsuserportal.twilio.controller;

import com.numbericsuserportal.twilio.dto.WhatsAppWebhookRequest;
import com.numbericsuserportal.twilio.service.WhatsAppMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Twilio Webhook Controller for receiving incoming WhatsApp messages
 * This endpoint is called by Twilio when a WhatsApp message is received
 * 
 * Note: This endpoint should NOT require authentication as Twilio will call it directly
 */
@RestController
@RequestMapping("/webhooks")
@CrossOrigin(origins = "*")
public class TwilioWebhookController {

    @Autowired
    private WhatsAppMessageService whatsAppMessageService;

    /**
     * Handle incoming WhatsApp messages from Twilio
     * 
     * POST /webhooks/whatsapp
     * 
     * Twilio sends form-encoded data (application/x-www-form-urlencoded)
     * with parameters: From, Body, MessageSid, AccountSid, To, NumMedia
     * 
     * @param request Incoming webhook request from Twilio
     * @return 200 OK response (Twilio expects a valid HTTP response)
     */
    @PostMapping("/whatsapp")
    public ResponseEntity<String> handleWhatsAppWebhook(
            @ModelAttribute WhatsAppWebhookRequest request) {
        
        // Log incoming message details
        System.out.println("=== Incoming Twilio WhatsApp Webhook ===");
        System.out.println("From: " + request.getFrom());
        System.out.println("Body: " + request.getBody());
        System.out.println("MessageSid: " + request.getMessageSid());
        System.out.println("AccountSid: " + request.getAccountSid());
        System.out.println("To: " + request.getTo());
        System.out.println("NumMedia: " + request.getNumMedia());
        System.out.println("MediaUrl0: " + request.getMediaUrl0());
        System.out.println("MediaContentType0: " + request.getMediaContentType0());
        System.out.println("=========================================");
        
        // Process and save the message using service layer
        try {
            whatsAppMessageService.saveIncomingMessage(
                    request.getFrom(),
                    request.getBody(),
                    request.getMessageSid(),
                    request.getTo(),
                    request.getNumMedia(),
                    request.getMediaUrl0(),
                    request.getMediaContentType0()
            );
            
            System.out.println("Message processed and saved successfully");
        } catch (Exception e) {
            System.err.println("Error processing WhatsApp message: " + e.getMessage());
            e.printStackTrace();
            // Still return 200 OK to Twilio to avoid retries
            // You may want to log this error for monitoring
        }
        
        // Return 200 OK - Twilio expects a valid HTTP response
        // You can return empty string or a simple message
        return ResponseEntity.ok("Message received");
    }
}

