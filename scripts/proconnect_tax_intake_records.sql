-- Tax intake OCR/sheet records (shared by ALL softwares).
-- software_name: PROCONNECT | DRAKE | PROSERIES | TAXWISE | CCH_AXCESS | LACERTE
-- Run on deploy BEFORE first traffic if table is missing.

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

-- Safe upgrade if an older table exists without software_name
SET @col_exists := (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'proconnect_tax_intake_records'
      AND COLUMN_NAME = 'software_name'
);
SET @sql := IF(@col_exists = 0,
    'ALTER TABLE proconnect_tax_intake_records ADD COLUMN software_name VARCHAR(40) NOT NULL DEFAULT ''PROCONNECT'' AFTER user_id',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Optional composite index for list-by-software
SET @idx_exists := (
    SELECT COUNT(*) FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'proconnect_tax_intake_records'
      AND INDEX_NAME = 'idx_tax_intake_user_software'
);
SET @sql2 := IF(@idx_exists = 0,
    'CREATE INDEX idx_tax_intake_user_software ON proconnect_tax_intake_records (user_id, software_name)',
    'SELECT 1');
PREPARE stmt2 FROM @sql2;
EXECUTE stmt2;
DEALLOCATE PREPARE stmt2;
