-- Create whatsapp_messages table for storing incoming and outgoing WhatsApp messages from Twilio
-- This table stores messages with optional user association

CREATE TABLE IF NOT EXISTS `whatsapp_messages` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `direction` VARCHAR(10) NOT NULL DEFAULT 'INCOMING' COMMENT 'Message direction: INCOMING (from user) or OUTGOING (to user)',
    `from_number` VARCHAR(20) NOT NULL COMMENT 'Phone number that sent the message (normalized, without whatsapp: prefix). For INCOMING: user phone. For OUTGOING: Twilio WhatsApp number.',
    `message_body` TEXT COMMENT 'Message body/content',
    `message_sid` VARCHAR(50) NULL COMMENT 'Twilio Message SID (unique identifier). NULL allowed for outgoing messages that may not have SID immediately.',
    `to_number` VARCHAR(20) COMMENT 'Phone number that received the message (normalized, without whatsapp: prefix). For INCOMING: Twilio WhatsApp number. For OUTGOING: user phone.',
    `num_media` VARCHAR(10) COMMENT 'Number of media items in the message',
    `received_at` DATETIME NOT NULL COMMENT 'Timestamp when the message was received/sent. For INCOMING: received time. For OUTGOING: sent time.',
    `user_id` BIGINT NULL COMMENT 'Foreign key to users.user_id (nullable - message can exist without a user)',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_message_sid` (`message_sid`),
    KEY `idx_from_number` (`from_number`),
    KEY `idx_to_number` (`to_number`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_received_at` (`received_at`),
    KEY `idx_direction` (`direction`),
    KEY `idx_direction_phone` (`direction`, `from_number`),
    CONSTRAINT `fk_whatsapp_messages_user` 
        FOREIGN KEY (`user_id`) 
        REFERENCES `users` (`user_id`) 
        ON DELETE SET NULL 
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci 
COMMENT='Stores incoming and outgoing WhatsApp messages from Twilio with optional user association';

