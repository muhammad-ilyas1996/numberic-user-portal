package com.numbericsuserportal.twilio.service;

import com.numbericsuserportal.twilio.config.TwilioConfig;
import com.numbericsuserportal.twilio.entity.TaxCase;
import com.numbericsuserportal.twilio.entity.TaxDocument;
import com.numbericsuserportal.twilio.entity.WhatsAppMessage;
import com.numbericsuserportal.twilio.repository.TaxDocumentRepository;
import com.numbericsuserportal.twilio.repository.WhatsAppMessageRepository;
import com.numbericsuserportal.twilio.service.TaxCaseService;
import com.numbericsuserportal.usermanagement.domain.User;
import com.numbericsuserportal.usermanagement.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service for handling WhatsApp message operations
 */
@Service
public class WhatsAppMessageService {

    @Autowired
    private WhatsAppMessageRepository whatsAppMessageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TwilioService twilioService;

    @Autowired
    private TaxCaseService taxCaseService;

    @Autowired
    private TwilioConfig twilioConfig;

    @Autowired
    private TaxDocumentRepository taxDocumentRepository;

    /**
     * Process and save incoming WhatsApp message
     * 
     * Steps:
     * 1. Normalize the "From" phone number (remove "whatsapp:" prefix)
     * 2. Find user by phone number
     * 3. Save message with user_id if found, null if not found
     * 
     * @param fromNumber Phone number from Twilio (may include "whatsapp:" prefix)
     * @param messageBody Message content
     * @param messageSid Twilio Message SID
     * @param toNumber Recipient phone number
     * @param numMedia Number of media items
     * @param mediaUrl0 URL of the first media item (if any)
     * @param mediaContentType0 Content type of the first media item (if any)
     * @return Saved WhatsAppMessage entity
     */
    @Transactional
    public WhatsAppMessage saveIncomingMessage(
            String fromNumber,
            String messageBody,
            String messageSid,
            String toNumber,
            String numMedia,
            String mediaUrl0,
            String mediaContentType0) {
        if (whatsAppMessageRepository.findByMessageSid(messageSid).isPresent()) {
            System.out.println("Duplicate message ignored: " + messageSid);
            return null;
        }

        // Step 1: Normalize phone number - remove "whatsapp:" prefix if present
        String normalizedFromNumber = normalizePhoneNumber(fromNumber);

        // Step 2: Find user by phone number
        Long userId = findUserIdByPhone(normalizedFromNumber);

        // Step 3: Create and save message entity (INCOMING)
        WhatsAppMessage message = new WhatsAppMessage();
        message.setDirection(WhatsAppMessage.MessageDirection.INCOMING);
        message.setFromNumber(normalizedFromNumber);
        message.setMessageBody(messageBody);
        message.setMessageSid(messageSid);
        message.setToNumber(toNumber);
        message.setNumMedia(numMedia);
        message.setReceivedAt(LocalDateTime.now());
        message.setUserId(userId); // Will be null if user not found

        WhatsAppMessage savedMessage = whatsAppMessageRepository.save(message);

        // Log the result
        if (userId != null) {
            System.out.println("Message saved with user_id: " + userId);
        } else {
            System.out.println("Message saved without user_id (user not found for phone: " + normalizedFromNumber + ")");
        }

        // Step 4: Process media if present
        if (numMedia != null && !numMedia.trim().isEmpty()) {
            try {
                int mediaCount = Integer.parseInt(numMedia.trim());
                if (mediaCount > 0 && mediaUrl0 != null && !mediaUrl0.trim().isEmpty()) {
                    processMediaDocument(normalizedFromNumber, mediaUrl0, mediaContentType0, userId);
                }
            } catch (NumberFormatException e) {
                System.err.println("Invalid NumMedia value: " + numMedia);
            } catch (Exception e) {
                System.err.println("Error processing media: " + e.getMessage());
                e.printStackTrace();
                // Don't fail the webhook if media processing fails
            }
        }

        // Step 5: Decide whether to send automatic reply and send it
        // This method handles TaxCase logic (higher priority) and keyword logic (fallback)
        // Note: Media processing may have updated TaxCase status, so reply logic runs after
        processAutomaticReply(normalizedFromNumber, messageBody, userId);

        return savedMessage;
    }

    /**
     * Process automatic reply decision and send reply if needed
     * 
     * Priority Logic:
     * 1. TaxCase Logic (Higher Priority):
     *    - Check if active TaxCase exists for phone number
     *    - If TaxCase exists and message contains "tax" keyword:
     *      - Create or update TaxCase
     *      - Send reply ONLY if TaxCase was newly created or status changed
     *    - If TaxCase exists but no "tax" keyword:
     *      - No reply sent (TaxCase exists but no trigger)
     * 
     * 2. Keyword Logic (Fallback):
     *    - Only used if no TaxCase exists or TaxCase processing fails
     *    - Check for keywords: "tax", "help"
     *    - Send keyword-based reply if keyword found
     * 
     * This method is safe for webhook execution - all exceptions are caught and logged.
     * 
     * @param phoneNumber Phone number (normalized, without "whatsapp:" prefix)
     * @param messageBody Original message content
     * @param userId User ID if available (can be null)
     */
    private void processAutomaticReply(String phoneNumber, String messageBody, Long userId) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            System.out.println("Cannot process automatic reply: phone number is empty");
            return;
        }

        // Priority 1: TaxCase Logic (Higher Priority)
        ReplyDecision decision = decideReplyBasedOnTaxCase(phoneNumber, messageBody, userId);
        
        if (decision.shouldSendReply()) {
            sendAutomaticReply(phoneNumber, messageBody, decision.getTaxCase(), decision.isNewlyCreated());
        } else if (decision.shouldUseKeywordFallback()) {
            // Priority 2: Keyword Logic (Fallback)
            String keywordReply = generateReplyMessage(messageBody);
            sendKeywordBasedReply(phoneNumber, keywordReply, userId);
        } else {
            System.out.println("No automatic reply sent - no TaxCase trigger and no keyword match");
        }
    }

    /**
     * Decide whether to send reply based on TaxCase logic
     * 
     * Returns a ReplyDecision object that indicates:
     * - Whether to send a reply
     * - Which TaxCase to use (if any)
     * - Whether TaxCase was newly created
     * - Whether to fall back to keyword logic
     * 
     * @param phoneNumber Phone number (normalized)
     * @param messageBody Message content
     * @param userId User ID if available
     * @return ReplyDecision with decision details
     */
    private ReplyDecision decideReplyBasedOnTaxCase(String phoneNumber, String messageBody, Long userId) {
        // Check if message contains "tax" keyword (required for TaxCase processing)
        boolean hasTaxKeyword = messageBody != null && messageBody.toLowerCase().contains("tax");
        
        if (!hasTaxKeyword) {
            // No "tax" keyword - check if TaxCase exists but don't process it
            try {
                TaxCase existingCase = taxCaseService.findActiveTaxCase(phoneNumber);
                if (existingCase != null) {
                    System.out.println("TaxCase exists but no 'tax' keyword - no reply sent");
                    return ReplyDecision.noReply();
                }
            } catch (Exception e) {
                System.err.println("Error checking existing TaxCase: " + e.getMessage());
                // Fall through to keyword logic
            }
            return ReplyDecision.keywordFallback();
        }

        // Message contains "tax" keyword - process TaxCase
        try {
            var taxCaseResult = taxCaseService.getOrCreateActiveTaxCaseWithResult(phoneNumber, userId);
            TaxCase taxCase = taxCaseResult.getTaxCase();
            
            System.out.println("TaxCase processed: ID=" + taxCase.getId() + 
                             ", Status=" + taxCase.getStatus() + 
                             ", Phone=" + taxCase.getPhoneNumber() +
                             ", NewlyCreated=" + taxCaseResult.isNewlyCreated() +
                             ", StatusChanged=" + taxCaseResult.isStatusChanged());
            
            // Send reply only if TaxCase was newly created or status changed
            if (taxCaseResult.isNewlyCreated() || taxCaseResult.isStatusChanged()) {
                System.out.println("Will send TaxCase-based reply: TaxCase was " + 
                                 (taxCaseResult.isNewlyCreated() ? "newly created" : "status changed"));
                return ReplyDecision.taxCaseReply(taxCase, taxCaseResult.isNewlyCreated());
            } else {
                System.out.println("Skipping reply: TaxCase exists with unchanged status");
                return ReplyDecision.noReply();
            }
        } catch (Exception e) {
            System.err.println("Error processing TaxCase: " + e.getMessage());
            e.printStackTrace();
            // Don't fail the webhook - fall back to keyword logic
            return ReplyDecision.keywordFallback();
        }
    }

    /**
     * Send automatic reply based on TaxCase status
     * 
     * This method is called only when TaxCase was newly created or status changed.
     * It uses status-based replies.
     * 
     * After sending reply for a newly created TaxCase (status NEW), automatically
     * updates the status to NEEDS_DOCUMENTS.
     * 
     * @param toPhoneNumber Phone number to send reply to (normalized, without "whatsapp:" prefix)
     * @param incomingMessage Original message content (for logging purposes)
     * @param taxCase TaxCase that triggered the reply
     * @param isNewlyCreated Whether the TaxCase was newly created
     */
    private void sendAutomaticReply(String toPhoneNumber, String incomingMessage, TaxCase taxCase, boolean isNewlyCreated) {
        if (toPhoneNumber == null || toPhoneNumber.trim().isEmpty()) {
            System.out.println("Cannot send automatic reply: phone number is empty");
            return;
        }

        if (incomingMessage == null) {
            incomingMessage = "";
        }

        // Generate reply message based on TaxCase status
        String replyMessage = generateReplyBasedOnTaxCaseStatus(taxCase.getStatus());

        try {
            // Send reply using TwilioService
            // Note: TwilioService expects E.164 format, which our normalized number should already be
            var response = twilioService.sendWhatsApp(toPhoneNumber, replyMessage);

            if (response.isSuccess()) {
                System.out.println("Automatic reply sent successfully to " + toPhoneNumber);
                System.out.println("Reply message: " + replyMessage);
                
                // Save outgoing message to database
                saveOutgoingMessage(toPhoneNumber, replyMessage, response.getMessageSid(), taxCase.getUserId());
                
                // After sending reply for a newly created TaxCase (status NEW),
                // automatically update status to NEEDS_DOCUMENTS
                if (isNewlyCreated && taxCase.getStatus() == TaxCase.TaxCaseStatus.NEW) {
                    try {
                        TaxCase updatedCase = taxCaseService.updateTaxCaseStatus(
                            taxCase.getId(), 
                            TaxCase.TaxCaseStatus.NEEDS_DOCUMENTS
                        );
                        System.out.println("TaxCase status automatically updated from NEW to NEEDS_DOCUMENTS. " +
                                         "TaxCase ID: " + updatedCase.getId());
                    } catch (Exception e) {
                        System.err.println("Error updating TaxCase status after reply: " + e.getMessage());
                        e.printStackTrace();
                        // Don't throw exception - status update failure shouldn't break the flow
                    }
                }
            } else {
                System.err.println("Failed to send automatic reply: " + response.getError());
            }
        } catch (Exception e) {
            System.err.println("Error sending automatic reply: " + e.getMessage());
            e.printStackTrace();
            // Don't throw exception - we don't want to fail the webhook if reply fails
        }
    }

    /**
     * Send keyword-based reply (fallback when TaxCase logic doesn't apply)
     * 
     * @param toPhoneNumber Phone number to send reply to
     * @param replyMessage Generated reply message
     * @param userId User ID if available
     */
    private void sendKeywordBasedReply(String toPhoneNumber, String replyMessage, Long userId) {
        if (toPhoneNumber == null || toPhoneNumber.trim().isEmpty()) {
            System.out.println("Cannot send keyword-based reply: phone number is empty");
            return;
        }

        if (replyMessage == null || replyMessage.trim().isEmpty()) {
            System.out.println("Cannot send keyword-based reply: message is empty");
            return;
        }

        try {
            var response = twilioService.sendWhatsApp(toPhoneNumber, replyMessage);

            if (response.isSuccess()) {
                System.out.println("Keyword-based reply sent successfully to " + toPhoneNumber);
                System.out.println("Reply message: " + replyMessage);
                
                // Save outgoing message to database
                saveOutgoingMessage(toPhoneNumber, replyMessage, response.getMessageSid(), userId);
            } else {
                System.err.println("Failed to send keyword-based reply: " + response.getError());
            }
        } catch (Exception e) {
            System.err.println("Error sending keyword-based reply: " + e.getMessage());
            e.printStackTrace();
            // Don't throw exception - we don't want to fail the webhook if reply fails
        }
    }

    /**
     * Generate reply message based on TaxCase status
     * 
     * @param status TaxCase status
     * @return Status-based reply message
     */
    private String generateReplyBasedOnTaxCaseStatus(TaxCase.TaxCaseStatus status) {
        if (status == null) {
            // Fallback to default message if status is null
            return "Thank you for contacting us! We have received your message and will get back to you soon.";
        }

        return switch (status) {
            case NEW -> 
                "Thank you for your interest in our tax services! " +
                "To get started, please upload your tax documents. " +
                "You can send photos or files of your tax forms, receipts, and any other relevant documents. " +
                "Once we receive your documents, we'll review them and proceed with your tax filing.";

            case NEEDS_DOCUMENTS -> 
                "We're still waiting for your tax documents to proceed with your filing. " +
                "Please upload the pending documents as soon as possible. " +
                "If you have any questions about which documents are needed, please let us know.";

            case IN_PROGRESS -> 
                "Great news! Your tax filing is currently in progress. " +
                "Our team is working on your case and will update you once it's completed. " +
                "If you have any questions or need to provide additional information, please don't hesitate to contact us.";
        };
    }

    /**
     * Generate reply message based on rule-based logic
     * 
     * Rules:
     * - If message contains "tax" (case-insensitive) → reply asking for tax documents
     * - If message contains "help" (case-insensitive) → reply with support message
     * - Otherwise → generic welcome message
     * 
     * @param incomingMessage Original message content
     * @return Generated reply message
     */
    private String generateReplyMessage(String incomingMessage) {
        if (incomingMessage == null) {
            incomingMessage = "";
        }

        String lowerMessage = incomingMessage.toLowerCase().trim();

        // Rule 1: Check for "tax" keyword
        if (lowerMessage.contains("tax")) {
            return "Thank you for your message about taxes. " +
                   "Please provide your tax documents, and we'll assist you with your tax-related inquiries. " +
                   "You can upload documents or share more details about what you need help with.";
        }

        // Rule 2: Check for "help" keyword
        if (lowerMessage.contains("help")) {
            return "Hello! We're here to help you. " +
                   "Our support team is available to assist with your questions. " +
                   "Please let us know what you need help with, and we'll get back to you shortly.";
        }

        // Rule 3: Default welcome message
        return "Thank you for contacting us! " +
               "We have received your message and will get back to you soon. " +
               "If you need immediate assistance, please type 'help' or 'tax' for specific support.";
    }

    /**
     * Normalize phone number by removing "whatsapp:" prefix
     * 
     * @param phoneNumber Phone number (may include "whatsapp:" prefix)
     * @return Normalized phone number without prefix
     */
    private String normalizePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return phoneNumber;
        }

        // Remove "whatsapp:" prefix if present (case-insensitive)
        String normalized = phoneNumber.trim();
        if (normalized.toLowerCase().startsWith("whatsapp:")) {
            normalized = normalized.substring(9); // Remove "whatsapp:" (9 characters)
        }

        return normalized.trim();
    }

    /**
     * Find user ID by phone number
     * 
     * @param phoneNumber Normalized phone number
     * @return User ID if found, null otherwise
     */
    private Long findUserIdByPhone(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return null;
        }

        // Find user by phone number
        // Note: Phone numbers might be stored in different formats, so we'll do exact match
        // You may want to normalize phone numbers in the users table for better matching
        return userRepository.findByPhone(phoneNumber)
                .map(User::getUserId)
                .orElse(null);
    }

    /**
     * Save outgoing WhatsApp message to database
     * 
     * @param toPhoneNumber Recipient phone number (normalized, without "whatsapp:" prefix)
     * @param messageBody Message content
     * @param messageSid Twilio Message SID (from response, can be null)
     * @param userId User ID if available (can be null)
     */
    @Transactional
    private void saveOutgoingMessage(String toPhoneNumber, String messageBody, String messageSid, Long userId) {
        try {
            // Get our Twilio WhatsApp number (normalized)
            String fromNumber = twilioConfig.getWhatsappNumber();
            if (fromNumber == null || fromNumber.isEmpty()) {
                System.err.println("Cannot save outgoing message: Twilio WhatsApp number not configured");
                return;
            }

            // Normalize the from number (remove "whatsapp:" prefix if present)
            String normalizedFromNumber = normalizePhoneNumber(fromNumber);

            // Create and save outgoing message entity
            WhatsAppMessage outgoingMessage = new WhatsAppMessage();
            outgoingMessage.setDirection(WhatsAppMessage.MessageDirection.OUTGOING);
            outgoingMessage.setFromNumber(normalizedFromNumber); // Our Twilio WhatsApp number
            outgoingMessage.setToNumber(toPhoneNumber); // Recipient's phone number
            outgoingMessage.setMessageBody(messageBody);
            outgoingMessage.setMessageSid(messageSid); // Can be null if not available
            outgoingMessage.setNumMedia(null); // Outgoing messages typically don't have media count
            outgoingMessage.setReceivedAt(LocalDateTime.now()); // Actually "sent_at" but using same column
            outgoingMessage.setUserId(userId); // Link to same user_id if available

            WhatsAppMessage savedOutgoingMessage = whatsAppMessageRepository.save(outgoingMessage);
            System.out.println("Outgoing message saved: ID=" + savedOutgoingMessage.getId() + 
                             ", To=" + toPhoneNumber + 
                             (userId != null ? ", UserID=" + userId : ", No User"));

        } catch (Exception e) {
            System.err.println("Error saving outgoing message: " + e.getMessage());
            e.printStackTrace();
            // Don't throw exception - message saving failure shouldn't break the flow
        }
    }

    /**
     * Inner class to encapsulate reply decision logic
     * 
     * This class holds the decision about whether to send a reply,
     * which type of reply to send, and related metadata.
     */
    private static class ReplyDecision {
        private final boolean shouldSendReply;
        private final boolean shouldUseKeywordFallback;
        private final TaxCase taxCase;
        private final boolean isNewlyCreated;

        private ReplyDecision(boolean shouldSendReply, boolean shouldUseKeywordFallback, 
                            TaxCase taxCase, boolean isNewlyCreated) {
            this.shouldSendReply = shouldSendReply;
            this.shouldUseKeywordFallback = shouldUseKeywordFallback;
            this.taxCase = taxCase;
            this.isNewlyCreated = isNewlyCreated;
        }

        /**
         * Create decision to send TaxCase-based reply
         */
        static ReplyDecision taxCaseReply(TaxCase taxCase, boolean isNewlyCreated) {
            return new ReplyDecision(true, false, taxCase, isNewlyCreated);
        }

        /**
         * Create decision to use keyword-based fallback
         */
        static ReplyDecision keywordFallback() {
            return new ReplyDecision(false, true, null, false);
        }

        /**
         * Create decision to not send any reply
         */
        static ReplyDecision noReply() {
            return new ReplyDecision(false, false, null, false);
        }

        boolean shouldSendReply() {
            return shouldSendReply;
        }

        boolean shouldUseKeywordFallback() {
            return shouldUseKeywordFallback;
        }

        TaxCase getTaxCase() {
            return taxCase;
        }

        boolean isNewlyCreated() {
            return isNewlyCreated;
        }
    }

    /**
     * Process media/document received via WhatsApp
     * 
     * Steps:
     * 1. Find active TaxCase for the phone number
     * 2. Save media as TaxDocument
     * 3. If TaxCase status is NEEDS_DOCUMENTS, update it to IN_PROGRESS
     * 4. Send automatic reply confirming document receipt
     * 
     * This method is safe for webhook execution - all exceptions are caught and logged.
     * 
     * @param phoneNumber Phone number (normalized, without "whatsapp:" prefix)
     * @param mediaUrl URL of the media/document from Twilio
     * @param contentType Content type of the media (e.g., image/jpeg, application/pdf)
     * @param userId User ID if available (can be null)
     */
    @Transactional
    private void processMediaDocument(String phoneNumber, String mediaUrl, String contentType, Long userId) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            System.out.println("Cannot process media: phone number is empty");
            return;
        }

        if (mediaUrl == null || mediaUrl.trim().isEmpty()) {
            System.out.println("Cannot process media: media URL is empty");
            return;
        }

        try {
            // Step 1: Find active TaxCase for the phone number
            TaxCase taxCase = taxCaseService.findActiveTaxCase(phoneNumber);

            if (taxCase == null) {
                System.out.println("No active TaxCase found for phone: " + phoneNumber + 
                                 " - media document not saved");
                return;
            }

            System.out.println("Processing media for TaxCase ID: " + taxCase.getId() + 
                             ", Phone: " + phoneNumber);

            // Step 2: Save media as TaxDocument
            TaxDocument taxDocument = new TaxDocument();
            taxDocument.setTaxCaseId(taxCase.getId());
            taxDocument.setMediaUrl(mediaUrl.trim());
            taxDocument.setContentType(contentType != null ? contentType.trim() : null);
            taxDocument.setUploadedAt(LocalDateTime.now());

            TaxDocument savedDocument = taxDocumentRepository.save(taxDocument);
            System.out.println("TaxDocument saved: ID=" + savedDocument.getId() + 
                             ", TaxCase ID=" + taxCase.getId() + 
                             ", Media URL=" + mediaUrl);

            // Step 3: If TaxCase status is NEEDS_DOCUMENTS, update it to IN_PROGRESS
            if (taxCase.getStatus() == TaxCase.TaxCaseStatus.NEEDS_DOCUMENTS) {
                try {
                    TaxCase updatedCase = taxCaseService.updateTaxCaseStatus(
                        taxCase.getId(), 
                        TaxCase.TaxCaseStatus.IN_PROGRESS
                    );
                    System.out.println("TaxCase status updated from NEEDS_DOCUMENTS to IN_PROGRESS. " +
                                     "TaxCase ID: " + updatedCase.getId());
                } catch (Exception e) {
                    System.err.println("Error updating TaxCase status after media upload: " + e.getMessage());
                    e.printStackTrace();
                    // Don't throw exception - status update failure shouldn't break the flow
                }
            }

            // Step 4: Send automatic reply confirming document receipt
            String replyMessage = "Documents received successfully. Your tax filing is now in progress.";
            sendMediaConfirmationReply(phoneNumber, replyMessage, userId);

        } catch (Exception e) {
            System.err.println("Error processing media document: " + e.getMessage());
            e.printStackTrace();
            // Don't throw exception - we don't want to fail the webhook if media processing fails
        }
    }

    /**
     * Send automatic reply confirming document receipt
     * 
     * @param toPhoneNumber Phone number to send reply to (normalized)
     * @param replyMessage Reply message content
     * @param userId User ID if available (can be null)
     */
    private void sendMediaConfirmationReply(String toPhoneNumber, String replyMessage, Long userId) {
        if (toPhoneNumber == null || toPhoneNumber.trim().isEmpty()) {
            System.out.println("Cannot send media confirmation reply: phone number is empty");
            return;
        }

        if (replyMessage == null || replyMessage.trim().isEmpty()) {
            System.out.println("Cannot send media confirmation reply: message is empty");
            return;
        }

        try {
            var response = twilioService.sendWhatsApp(toPhoneNumber, replyMessage);

            if (response.isSuccess()) {
                System.out.println("Media confirmation reply sent successfully to " + toPhoneNumber);
                System.out.println("Reply message: " + replyMessage);
                
                // Save outgoing message to database
                saveOutgoingMessage(toPhoneNumber, replyMessage, response.getMessageSid(), userId);
            } else {
                System.err.println("Failed to send media confirmation reply: " + response.getError());
            }
        } catch (Exception e) {
            System.err.println("Error sending media confirmation reply: " + e.getMessage());
            e.printStackTrace();
            // Don't throw exception - we don't want to fail the webhook if reply fails
        }
    }
}

