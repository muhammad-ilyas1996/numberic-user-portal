-- Remove processing_status and entry_type from receipts table
-- These fields have been moved to receipt_data table

ALTER TABLE receipts 
DROP COLUMN IF EXISTS processing_status,
DROP COLUMN IF EXISTS entry_type;
