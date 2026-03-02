-- Payment & transactions: each invoice payment (e.g. via NMI). Optional if using JPA ddl-auto=update.
CREATE TABLE IF NOT EXISTS `payment_transaction` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `invoice_id` BIGINT NOT NULL,
    `invoice_num` VARCHAR(50) NULL COMMENT 'For display in list',
    `amount` DOUBLE NOT NULL,
    `currency` VARCHAR(10) NULL DEFAULT 'USD',
    `gateway` VARCHAR(50) NOT NULL COMMENT 'NMI, STRIPE, etc.',
    `gateway_transaction_id` VARCHAR(100) NULL,
    `auth_code` VARCHAR(50) NULL,
    `status` VARCHAR(20) NOT NULL COMMENT 'SUCCESS, FAILED',
    `paid_at` DATETIME NOT NULL,
    `payer_email` VARCHAR(255) NULL,
    `description` VARCHAR(500) NULL,
    `created_on` DATETIME NULL,
    PRIMARY KEY (`id`),
    KEY `idx_payment_transaction_invoice_id` (`invoice_id`),
    KEY `idx_payment_transaction_paid_at` (`paid_at`),
    KEY `idx_payment_transaction_status` (`status`)
);
