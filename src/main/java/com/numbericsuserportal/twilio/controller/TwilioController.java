package com.numbericsuserportal.twilio.controller;

import com.numbericsuserportal.twilio.dto.SendWhatsAppRequest;
import com.numbericsuserportal.twilio.dto.TwilioResponse;
import com.numbericsuserportal.twilio.service.TwilioService;
import com.numbericsuserportal.usermanagement.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Twilio Controller for WhatsApp messaging
 * Uses existing login JWT token for authentication
 * No permission checks - any authenticated user can send WhatsApp messages
 */
@RestController
@RequestMapping("/api/twilio")
@CrossOrigin(origins = "*")
public class TwilioController {

    @Autowired
    private TwilioService twilioService;

    /**
     * Send WhatsApp message
     * Uses existing login token - no need to generate new token
     * 
     * POST /api/twilio/send-whatsapp
     * Headers: Authorization: Bearer <LOGIN_TOKEN>
     * 
     * @param request WhatsApp message request (to, message)
     * @param currentUser Current authenticated user (from JWT token)
     * @return TwilioResponse with success status and message SID
     */
    @PostMapping("/send-whatsapp")
    public ResponseEntity<TwilioResponse> sendWhatsApp(
            @RequestBody SendWhatsAppRequest request,
            @AuthenticationPrincipal User currentUser) {
        
        // Optional: Log which user is sending the message
        if (currentUser != null) {
            System.out.println("User " + currentUser.getUsername() + 
                " (ID: " + currentUser.getUserId() + ") is sending WhatsApp message to " + request.getTo());
        }
        
        TwilioResponse response = twilioService.sendWhatsApp(
            request.getTo(), 
            request.getMessage()
        );
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Health check endpoint
     * GET /api/twilio/health
     * 
     * @return Health status message
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Twilio WhatsApp service is running");
    }
}

