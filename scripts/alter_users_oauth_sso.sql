-- Google / LinkedIn SSO columns on users
-- Compatible with MySQL 5.7 / 8.0 (no IF NOT EXISTS on ADD COLUMN / CREATE INDEX).
-- Run once. If a column/index already exists, skip that statement and continue.

-- 1) Columns (run as one statement, or one-by-one if some already exist)
ALTER TABLE users
  ADD COLUMN auth_provider VARCHAR(20) NOT NULL DEFAULT 'LOCAL';

ALTER TABLE users
  ADD COLUMN google_id VARCHAR(128) NULL;

ALTER TABLE users
  ADD COLUMN linkedin_id VARCHAR(128) NULL;

-- 2) Unique indexes (skip if error "Duplicate key name")
CREATE UNIQUE INDEX uk_users_google_id ON users (google_id);
CREATE UNIQUE INDEX uk_users_linkedin_id ON users (linkedin_id);
