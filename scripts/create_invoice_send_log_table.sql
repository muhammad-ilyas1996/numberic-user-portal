-- Invoice send history (WhatsApp / Email). Run if not using JPA ddl-auto.
CREATE TABLE IF NOT EXISTS `invoice_send_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `invoice_id` BIGINT NOT NULL,
    `channel` VARCHAR(20) NOT NULL COMMENT 'WHATSAPP, EMAIL',
    `sent_to` VARCHAR(255) NOT NULL COMMENT 'Phone (E.164) or email',
    `sent_at` DATETIME NOT NULL,
    `token` VARCHAR(64) NULL COMMENT 'Payment link token',
    `message_sid` VARCHAR(50) NULL COMMENT 'Twilio message SID',
    `created_by` VARCHAR(64) NULL,
    `created_on` DATETIME NULL,
    PRIMARY KEY (`id`),
    KEY `idx_invoice_send_log_invoice_id` (`invoice_id`)
);
