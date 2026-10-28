-- Google / LinkedIn SSO columns on users
-- Run once against the Numbrics DB before deploying OAuth auth APIs.

ALTER TABLE users
  ADD COLUMN IF NOT EXISTS auth_provider VARCHAR(20) NOT NULL DEFAULT 'LOCAL',
  ADD COLUMN IF NOT EXISTS google_id VARCHAR(128) NULL,
  ADD COLUMN IF NOT EXISTS linkedin_id VARCHAR(128) NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uk_users_google_id ON users (google_id);
CREATE UNIQUE INDEX IF NOT EXISTS uk_users_linkedin_id ON users (linkedin_id);
