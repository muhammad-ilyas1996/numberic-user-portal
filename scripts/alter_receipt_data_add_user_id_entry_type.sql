-- Add user_id and entry_type to receipt_data table
-- These fields were moved from receipts table

ALTER TABLE receipt_data 
ADD COLUMN IF NOT EXISTS user_id BIGINT NOT NULL AFTER receipt_id,
ADD COLUMN IF NOT EXISTS entry_type VARCHAR(20) DEFAULT 'OCR' AFTER user_id;

-- Add index for user_id for better query performance
CREATE INDEX IF NOT EXISTS idx_receipt_data_user_id ON receipt_data(user_id);

-- Add index for entry_type for filtering
CREATE INDEX IF NOT EXISTS idx_receipt_data_entry_type ON receipt_data(entry_type);

-- Note: If you want to add foreign key constraint, uncomment below (adjust table name if different)
-- ALTER TABLE receipt_data 
-- ADD CONSTRAINT fk_receipt_data_user_id 
-- FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE;
