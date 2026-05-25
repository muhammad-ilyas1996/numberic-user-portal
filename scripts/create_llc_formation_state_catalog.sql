-- NW State Filing Fees 2026 catalog (50 states + DC)

CREATE TABLE IF NOT EXISTS llc_formation_state_catalog (
  id BIGINT NOT NULL AUTO_INCREMENT,
  state_name VARCHAR(64) NOT NULL,
  state_code VARCHAR(10) NOT NULL,
  filing_fee_cents INT NOT NULL,
  annual_report_fee VARCHAR(128) NULL,
  processing_time VARCHAR(64) NULL,
  speed VARCHAR(16) NULL,
  notes TEXT NULL,
  nw_service_fee_cents INT NOT NULL DEFAULT 3900,
  nw_ra_year1_cents INT NOT NULL DEFAULT 0,
  nw_ra_renewal_cents INT NOT NULL DEFAULT 12500,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_llc_formation_state_catalog_code (state_code),
  INDEX idx_llc_formation_state_catalog_active (active)
);
