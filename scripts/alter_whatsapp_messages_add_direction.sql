-- Alter whatsapp_messages table to add direction column for existing tables
-- Run this script if the table already exists

ALTER TABLE `whatsapp_messages` 
ADD COLUMN `direction` VARCHAR(10) NOT NULL DEFAULT 'INCOMING' 
COMMENT 'Message direction: INCOMING (from user) or OUTGOING (to user)' 
AFTER `id`;

-- Update existing records to be INCOMING (they are all incoming messages)
UPDATE `whatsapp_messages` SET `direction` = 'INCOMING' WHERE `direction` IS NULL OR `direction` = '';

-- Make message_sid nullable (outgoing messages may not have SID immediately)
ALTER TABLE `whatsapp_messages` 
MODIFY COLUMN `message_sid` VARCHAR(50) NULL 
COMMENT 'Twilio Message SID (unique identifier). NULL allowed for outgoing messages.';

-- Remove unique constraint on message_sid (since it can be null and we may have duplicates for outgoing)
-- Note: We'll handle uniqueness in application logic for non-null message_sid values
ALTER TABLE `whatsapp_messages` 
DROP INDEX IF EXISTS `uk_message_sid`;

-- Add unique constraint only for non-null message_sid values
-- MySQL doesn't support partial unique indexes directly, so we'll add a regular unique index
-- which allows multiple NULL values but enforces uniqueness for non-NULL values
ALTER TABLE `whatsapp_messages` 
ADD UNIQUE KEY `uk_message_sid` (`message_sid`);

-- Add indexes for better query performance
ALTER TABLE `whatsapp_messages` 
ADD KEY `idx_to_number` (`to_number`),
ADD KEY `idx_direction` (`direction`),
ADD KEY `idx_direction_phone` (`direction`, `from_number`);

