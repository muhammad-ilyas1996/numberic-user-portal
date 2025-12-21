package com.numbericsuserportal.twilio.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity for storing incoming and outgoing WhatsApp messages from Twilio
 */
@Entity
@Table(name = "whatsapp_messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WhatsAppMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * Message direction: INCOMING (from user) or OUTGOING (to user)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "direction", nullable = false, length = 10)
    private MessageDirection direction;

    /**
     * Phone number that sent the message (normalized, without "whatsapp:" prefix)
     * For INCOMING: user's phone number
     * For OUTGOING: your Twilio WhatsApp number
     */
    @Column(name = "from_number", nullable = false, length = 20)
    private String fromNumber;

    /**
     * Message body/content
     */
    @Column(name = "message_body", columnDefinition = "TEXT")
    private String messageBody;

    /**
     * Twilio Message SID (unique identifier)
     * Note: For outgoing messages, this may need to be nullable or use a different uniqueness constraint
     */
    @Column(name = "message_sid", length = 50)
    private String messageSid;

    /**
     * Phone number that received the message (normalized, without "whatsapp:" prefix)
     * For INCOMING: your Twilio WhatsApp number
     * For OUTGOING: user's phone number
     */
    @Column(name = "to_number", length = 20)
    private String toNumber;

    /**
     * Number of media items in the message
     */
    @Column(name = "num_media", length = 10)
    private String numMedia;

    /**
     * Timestamp when the message was received/sent
     * For INCOMING: when message was received
     * For OUTGOING: when message was sent
     */
    @Column(name = "received_at", nullable = false)
    private LocalDateTime receivedAt;

    /**
     * Foreign key to users table (nullable - message can exist without a user)
     */
    @Column(name = "user_id", nullable = true)
    private Long userId;

    @PrePersist
    protected void onCreate() {
        if (receivedAt == null) {
            receivedAt = LocalDateTime.now();
        }
        if (direction == null) {
            direction = MessageDirection.INCOMING; // Default to INCOMING for backward compatibility
        }
    }

    /**
     * Message Direction Enum
     */
    public enum MessageDirection {
        INCOMING,
        OUTGOING
    }
}

