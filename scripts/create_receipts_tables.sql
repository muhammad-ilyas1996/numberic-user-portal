-- Create receipts table
CREATE TABLE IF NOT EXISTS receipts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    image_path VARCHAR(500) NOT NULL,
    original_filename VARCHAR(255),
    uploaded_at TIMESTAMP NOT NULL,
    CREATED_BY VARCHAR(255) NOT NULL DEFAULT 'UNKNOWN',
    CREATED_ON TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    MODIFIED_BY VARCHAR(255),
    MODIFIED_ON TIMESTAMP,
    IS_ACTIVE BOOLEAN DEFAULT TRUE,
    INDEX idx_user_id (user_id),
    INDEX idx_uploaded_at (uploaded_at)
);

-- Create receipt_data table
CREATE TABLE IF NOT EXISTS receipt_data (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    receipt_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    entry_type VARCHAR(20) DEFAULT 'OCR',
    merchant_name VARCHAR(255),
    receipt_date DATE,
    total_amount DECIMAL(10,2),
    tax_amount DECIMAL(10,2),
    raw_text TEXT,
    confidence_score DOUBLE,
    category VARCHAR(255),
    status VARCHAR(255),
    extracted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (receipt_id) REFERENCES receipts(id) ON DELETE CASCADE,
    INDEX idx_receipt_id (receipt_id),
    INDEX idx_user_id (user_id),
    INDEX idx_entry_type (entry_type),
    INDEX idx_merchant_name (merchant_name),
    INDEX idx_receipt_date (receipt_date)
);
