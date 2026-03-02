-- Recurring invoice profile and line items. Optional if using JPA ddl-auto=update.
CREATE TABLE IF NOT EXISTS `recurring_invoice` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(255) NULL,
    `total_tax_amount_calculated` DOUBLE NULL,
    `taxable_amount` DOUBLE NULL,
    `nexus_met` VARCHAR(255) NULL,
    `tax_rate_calculated` DOUBLE NULL,
    `has_active_registration` VARCHAR(255) NULL,
    `transaction_items` VARCHAR(500) NULL,
    `external_id` VARCHAR(255) NULL,
    `currency` VARCHAR(20) NULL,
    `description` VARCHAR(500) NULL,
    `customer_name` VARCHAR(255) NULL,
    `customer_email` VARCHAR(255) NULL,
    `customer_street` VARCHAR(255) NULL,
    `customer_city` VARCHAR(100) NULL,
    `customer_state` VARCHAR(100) NULL,
    `customer_postal_code` VARCHAR(20) NULL,
    `customer_country` VARCHAR(100) NULL,
    `ship_street` VARCHAR(255) NULL,
    `ship_city` VARCHAR(100) NULL,
    `ship_state` VARCHAR(100) NULL,
    `ship_postal_code` VARCHAR(20) NULL,
    `ship_country` VARCHAR(100) NULL,
    `frequency` VARCHAR(20) NOT NULL,
    `start_date` DATE NOT NULL,
    `end_date` DATE NULL,
    `next_run_on` DATE NOT NULL,
    `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    `run_count` INT NOT NULL DEFAULT 0,
    `due_days` INT NULL DEFAULT 30,
    `created_by` VARCHAR(64) NULL,
    `created_on` DATETIME NULL,
    `modified_by` VARCHAR(64) NULL,
    `modified_on` DATETIME NULL,
    PRIMARY KEY (`id`),
    KEY `idx_recurring_invoice_status_next` (`status`, `next_run_on`)
);

CREATE TABLE IF NOT EXISTS `recurring_invoice_item` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `product_name` VARCHAR(255) NULL,
    `quantity` DOUBLE NULL,
    `amount` DOUBLE NULL,
    `description` VARCHAR(500) NULL,
    `recurring_invoice_id` BIGINT NOT NULL,
    PRIMARY KEY (`id`),
    KEY `fk_recurring_invoice_item_recurring` (`recurring_invoice_id`),
    CONSTRAINT `fk_recurring_invoice_item_recurring` FOREIGN KEY (`recurring_invoice_id`) REFERENCES `recurring_invoice` (`id`) ON DELETE CASCADE
);

-- Add column to invoice_tax if not using ddl-auto (optional)
-- ALTER TABLE `invoice_tax` ADD COLUMN `recurring_invoice_id` BIGINT NULL;
