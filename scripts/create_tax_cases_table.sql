-- Create tax_cases table for storing tax cases created from WhatsApp messages
-- One active TaxCase per phone number

CREATE TABLE IF NOT EXISTS `tax_cases` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NULL COMMENT 'Foreign key to users.user_id (nullable - case can exist without a user)',
    `phone_number` VARCHAR(20) NOT NULL COMMENT 'Phone number associated with this tax case',
    `status` VARCHAR(20) NOT NULL DEFAULT 'NEW' COMMENT 'Status of the tax case: NEW, NEEDS_DOCUMENTS, IN_PROGRESS',
    `created_at` DATETIME NOT NULL COMMENT 'Timestamp when the case was created',
    PRIMARY KEY (`id`),
    KEY `idx_phone_number` (`phone_number`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_phone_status` (`phone_number`, `status`),
    CONSTRAINT `fk_tax_cases_user` 
        FOREIGN KEY (`user_id`) 
        REFERENCES `users` (`user_id`) 
        ON DELETE SET NULL 
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci 
COMMENT='Stores tax cases created from WhatsApp messages. One active case per phone number.';

