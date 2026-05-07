-- LLC Formation tables (Northwest journey)

CREATE TABLE IF NOT EXISTS llc_formation (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  status VARCHAR(32) NOT NULL,
  jurisdiction VARCHAR(10) NULL,
  entity_type VARCHAR(20) NULL,
  ownership_type VARCHAR(20) NULL,
  operates_in_formation_state BOOLEAN NULL,
  operating_business_street VARCHAR(255) NULL,
  operating_business_city VARCHAR(255) NULL,
  operating_business_state VARCHAR(10) NULL,
  operating_business_zip VARCHAR(10) NULL,

  llc_name VARCHAR(255) NULL,
  alt_name VARCHAR(255) NULL,
  industry VARCHAR(255) NULL,
  business_purpose TEXT NULL,
  name_check_status VARCHAR(32) NULL,
  name_check_last_checked_at DATETIME NULL,

  owner_first_name VARCHAR(255) NULL,
  owner_last_name VARCHAR(255) NULL,
  owner_dob DATE NULL,
  owner_ssn_last4_enc VARCHAR(255) NULL,
  ownership_pct INT NULL,
  owner_title VARCHAR(255) NULL,
  management_type VARCHAR(16) NULL,
  address_same_as_home BOOLEAN NULL,
  business_street VARCHAR(255) NULL,
  business_city VARCHAR(255) NULL,
  business_state VARCHAR(10) NULL,
  business_zip VARCHAR(10) NULL,
  filing_speed VARCHAR(16) NULL,
  addon_ein BOOLEAN NULL,
  addon_scorp BOOLEAN NULL,

  numbrics_fee_cents INT NULL,
  state_fee_cents INT NULL,
  speed_fee_cents INT NULL,
  ein_fee_cents INT NULL,
  scorp_fee_cents INT NULL,
  total_cents INT NULL,

  stripe_customer_id VARCHAR(255) NULL,
  stripe_payment_intent_id VARCHAR(255) NULL,
  paid_at DATETIME NULL,

  company_id VARCHAR(64) NULL,
  filing_product_id VARCHAR(64) NULL,
  filing_method_id VARCHAR(64) NULL,
  filing_id VARCHAR(64) NULL,
  filing_reference VARCHAR(255) NULL,
  filing_status VARCHAR(255) NULL,
  northwest_shopping_cart_json TEXT NULL,
  northwest_checkout_completed_at DATETIME NULL,

  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,

  PRIMARY KEY (id),
  INDEX idx_llc_formation_user_id (user_id),
  INDEX idx_llc_formation_status (status)
);

CREATE TABLE IF NOT EXISTS llc_formation_registered_agent (
  id BIGINT NOT NULL AUTO_INCREMENT,
  formation_id BIGINT NOT NULL,
  agent_type VARCHAR(24) NOT NULL,
  source VARCHAR(24) NOT NULL,

  northwest_ref_id VARCHAR(255) NULL,
  agent_name_snapshot VARCHAR(255) NULL,
  agent_address_snapshot VARCHAR(255) NULL,

  agent_name VARCHAR(255) NULL,
  street VARCHAR(255) NULL,
  city VARCHAR(255) NULL,
  state VARCHAR(10) NULL,
  zip VARCHAR(10) NULL,

  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,

  PRIMARY KEY (id),
  UNIQUE KEY uk_llc_formation_registered_agent_formation (formation_id),
  INDEX idx_llc_formation_registered_agent_formation (formation_id),
  CONSTRAINT fk_llc_formation_registered_agent_formation
    FOREIGN KEY (formation_id) REFERENCES llc_formation(id)
    ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS llc_formation_rate (
  id BIGINT NOT NULL AUTO_INCREMENT,
  rate_type VARCHAR(32) NOT NULL,
  state_code VARCHAR(10) NULL,
  speed_code VARCHAR(16) NULL,
  amount_cents INT NOT NULL,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_llc_formation_rate_type_state_speed (rate_type, state_code, speed_code),
  INDEX idx_llc_formation_rate_type (rate_type),
  INDEX idx_llc_formation_rate_state (state_code)
);

CREATE TABLE IF NOT EXISTS llc_formation_member (
  id BIGINT NOT NULL AUTO_INCREMENT,
  formation_id BIGINT NOT NULL,
  first_name VARCHAR(255) NOT NULL,
  last_name VARCHAR(255) NOT NULL,
  dob DATE NULL,
  ssn_last4_enc VARCHAR(255) NULL,
  ownership_pct INT NOT NULL,
  title VARCHAR(255) NULL,
  is_primary BOOLEAN NOT NULL DEFAULT FALSE,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  PRIMARY KEY (id),
  INDEX idx_llc_formation_member_formation (formation_id),
  CONSTRAINT fk_llc_formation_member_formation
    FOREIGN KEY (formation_id) REFERENCES llc_formation(id)
    ON DELETE CASCADE
);

-- Existing databases: optional migration for columns added above
-- ALTER TABLE llc_formation ADD COLUMN IF NOT EXISTS northwest_shopping_cart_json TEXT NULL;
-- ALTER TABLE llc_formation ADD COLUMN IF NOT EXISTS northwest_checkout_completed_at DATETIME NULL;
