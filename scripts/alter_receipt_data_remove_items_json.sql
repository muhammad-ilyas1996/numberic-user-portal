-- Remove items_json column from receipt_data table (for existing databases)
ALTER TABLE receipt_data 
DROP COLUMN IF EXISTS items_json;
