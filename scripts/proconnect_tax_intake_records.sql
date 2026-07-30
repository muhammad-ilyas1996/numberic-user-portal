-- ProConnect tax intake OCR records
CREATE TABLE IF NOT EXISTS proconnect_tax_intake_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    software_name VARCHAR(40) NOT NULL DEFAULT 'PROCONNECT',
    original_file_name VARCHAR(260) NOT NULL,
    stored_file_path VARCHAR(500) NOT NULL,
    content_type VARCHAR(100) NULL,
    file_size BIGINT NULL,
    document_type VARCHAR(50) NULL,
    raw_text TEXT NULL,
    extracted_data_json TEXT NULL,
    overall_confidence DOUBLE NULL,
    page_count INT NULL,
    processing_status VARCHAR(20) NULL,
    error_message TEXT NULL,

    created_by VARCHAR(255) NOT NULL,
    created_on DATETIME NOT NULL,
    modified_by VARCHAR(255) NOT NULL,
    modified_on DATETIME NULL,
    is_active BIT(1) DEFAULT 1,

    INDEX idx_proconnect_tax_intake_user_created (user_id, created_on),
    INDEX idx_proconnect_tax_intake_status (processing_status)
);
