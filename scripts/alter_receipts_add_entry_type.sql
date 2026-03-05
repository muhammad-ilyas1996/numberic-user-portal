-- Add entry_type column to receipts table (for existing databases)
ALTER TABLE receipts 
ADD COLUMN IF NOT EXISTS entry_type VARCHAR(20) DEFAULT 'OCR';
