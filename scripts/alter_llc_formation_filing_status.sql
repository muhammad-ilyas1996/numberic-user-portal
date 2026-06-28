-- Extend filing_status for NW error messages (safe to re-run).
ALTER TABLE llc_formation
  MODIFY COLUMN filing_status VARCHAR(255) NULL;
