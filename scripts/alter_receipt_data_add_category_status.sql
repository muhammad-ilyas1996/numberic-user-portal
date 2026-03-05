-- Add category and status columns to receipt_data table (for existing databases)
ALTER TABLE receipt_data 
ADD COLUMN IF NOT EXISTS category VARCHAR(255),
ADD COLUMN IF NOT EXISTS status VARCHAR(255);
