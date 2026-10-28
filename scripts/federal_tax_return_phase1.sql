-- Phase 1 Federal Tax Return module (JPA can also auto-create if ddl-auto=update)

CREATE TABLE IF NOT EXISTS tax_preparer_profiles (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL UNIQUE,
  display_name_internal VARCHAR(120),
  active BOOLEAN NOT NULL DEFAULT TRUE,
  max_open_cases INT NOT NULL DEFAULT 25,
  qualifications VARCHAR(255) DEFAULT 'FORM_1040,SCHEDULE_C',
  created_at DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS federal_tax_return_cases (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  public_case_id VARCHAR(40) NOT NULL UNIQUE,
  taxpayer_user_id BIGINT NOT NULL,
  return_type VARCHAR(32) NOT NULL,
  service_option VARCHAR(32),
  client_status VARCHAR(40) NOT NULL,
  tax_year INT NOT NULL,
  organizer_json LONGTEXT,
  extracted_json LONGTEXT,
  validation_json LONGTEXT,
  missing_items_json LONGTEXT,
  assigned_preparer_user_id BIGINT,
  assigned_at DATETIME,
  reviewer_user_id BIGINT,
  reviewer_approved BOOLEAN NOT NULL DEFAULT FALSE,
  reviewer_approved_at DATETIME,
  clarification_open BOOLEAN NOT NULL DEFAULT FALSE,
  clarification_note VARCHAR(1000),
  export_package_path VARCHAR(500),
  form_8879_signed BOOLEAN NOT NULL DEFAULT FALSE,
  form_8879_signed_at DATETIME,
  spouse_8879_required BOOLEAN NOT NULL DEFAULT FALSE,
  spouse_8879_signed BOOLEAN NOT NULL DEFAULT FALSE,
  taxpayer_consent BOOLEAN NOT NULL DEFAULT FALSE,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  INDEX idx_ftx_taxpayer (taxpayer_user_id),
  INDEX idx_ftx_preparer (assigned_preparer_user_id),
  INDEX idx_ftx_status (client_status)
);

CREATE TABLE IF NOT EXISTS federal_tax_documents (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  case_id BIGINT NOT NULL,
  original_filename VARCHAR(255),
  stored_path VARCHAR(500),
  content_type VARCHAR(120),
  doc_type VARCHAR(64),
  ocr_text LONGTEXT,
  extracted_json LONGTEXT,
  uploaded_via VARCHAR(32),
  created_at DATETIME NOT NULL,
  INDEX idx_ftd_case (case_id)
);

CREATE TABLE IF NOT EXISTS form_8879_authorizations (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  case_id BIGINT NOT NULL,
  signer_role VARCHAR(32) NOT NULL,
  signer_user_id BIGINT,
  signer_name VARCHAR(160),
  consent_text VARCHAR(2000),
  signature_method VARCHAR(64),
  e_sign_provider VARCHAR(64),
  ip_address VARCHAR(64),
  user_agent VARCHAR(512),
  signed_at DATETIME NOT NULL,
  auth_token_hash VARCHAR(128),
  INDEX idx_8879_case (case_id)
);

CREATE TABLE IF NOT EXISTS federal_tax_case_audit_logs (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  case_id BIGINT NOT NULL,
  actor_user_id BIGINT,
  actor_role VARCHAR(40),
  action VARCHAR(80) NOT NULL,
  detail VARCHAR(2000),
  created_at DATETIME NOT NULL,
  INDEX idx_ftx_audit_case (case_id)
);
