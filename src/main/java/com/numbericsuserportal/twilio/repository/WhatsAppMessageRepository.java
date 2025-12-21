package com.numbericsuserportal.twilio.repository;

import com.numbericsuserportal.twilio.entity.WhatsAppMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for WhatsAppMessage entity
 */
@Repository
public interface WhatsAppMessageRepository extends JpaRepository<WhatsAppMessage, Long> {

    /**
     * Find message by Twilio Message SID
     */
    Optional<WhatsAppMessage> findByMessageSid(String messageSid);

    /**
     * Find all messages for a specific user
     */
    List<WhatsAppMessage> findByUserId(Long userId);

    /**
     * Find all messages from a specific phone number
     */
    List<WhatsAppMessage> findByFromNumber(String fromNumber);

    /**
     * Find all messages without an associated user
     */
    List<WhatsAppMessage> findByUserIdIsNull();

    /**
     * Find all messages by direction (INCOMING or OUTGOING)
     */
    List<WhatsAppMessage> findByDirection(WhatsAppMessage.MessageDirection direction);

    /**
     * Find all messages for a specific phone number by direction
     */
    List<WhatsAppMessage> findByFromNumberAndDirection(String fromNumber, WhatsAppMessage.MessageDirection direction);

    /**
     * Find all messages to a specific phone number by direction
     */
    List<WhatsAppMessage> findByToNumberAndDirection(String toNumber, WhatsAppMessage.MessageDirection direction);
}

