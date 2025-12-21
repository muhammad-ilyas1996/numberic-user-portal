-- Create tax_documents table for storing tax documents uploaded via WhatsApp
-- This table stores media URLs and metadata linked to TaxCase

CREATE TABLE IF NOT EXISTS `tax_documents` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `tax_case_id` BIGINT NOT NULL COMMENT 'Foreign key to tax_cases.id',
    `media_url` VARCHAR(500) NOT NULL COMMENT 'URL of the media/document from Twilio',
    `content_type` VARCHAR(100) COMMENT 'Content type of the media (e.g., image/jpeg, application/pdf)',
    `uploaded_at` DATETIME NOT NULL COMMENT 'Timestamp when the document was uploaded',
    PRIMARY KEY (`id`),
    KEY `idx_tax_case_id` (`tax_case_id`),
    KEY `idx_uploaded_at` (`uploaded_at`),
    CONSTRAINT `fk_tax_documents_tax_case` 
        FOREIGN KEY (`tax_case_id`) 
        REFERENCES `tax_cases` (`id`) 
        ON DELETE CASCADE 
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci 
COMMENT='Stores tax documents uploaded via WhatsApp, linked to TaxCase';

