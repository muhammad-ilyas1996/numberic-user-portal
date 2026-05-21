-- NMI reseller merchant onboarding support.
-- Safe to run multiple times on MySQL 8+.

CREATE TABLE IF NOT EXISTS `merchant_nmi_onboarding_application` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `status` VARCHAR(64) NOT NULL,
    `business_name` VARCHAR(255) NULL,
    `legal_name` VARCHAR(255) NULL,
    `contact_email` VARCHAR(255) NULL,
    `nmi_application_id` VARCHAR(128) NULL,
    `nmi_merchant_id` VARCHAR(128) NULL,
    `declined_reason` VARCHAR(1000) NULL,
    `safe_response_json` TEXT NULL,
    `created_on` DATETIME NULL,
    `updated_on` DATETIME NULL,
    `submitted_on` DATETIME NULL,
    `approved_on` DATETIME NULL,
    PRIMARY KEY (`id`),
    KEY `idx_nmi_onboarding_user_id` (`user_id`),
    KEY `idx_nmi_onboarding_application_id` (`nmi_application_id`),
    KEY `idx_nmi_onboarding_status` (`status`)
);

ALTER TABLE `merchant_nmi_config`
    ADD COLUMN IF NOT EXISTS `nmi_merchant_id` VARCHAR(128) NULL,
    ADD COLUMN IF NOT EXISTS `boarding_status` VARCHAR(64) NULL,
    ADD COLUMN IF NOT EXISTS `boarding_application_id` VARCHAR(128) NULL,
    ADD COLUMN IF NOT EXISTS `approved_at` DATETIME NULL;
