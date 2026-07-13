-- =============================================================================
-- Subscription catalog + user columns (run on MySQL before/after deploy)
-- =============================================================================

CREATE TABLE IF NOT EXISTS subscription_plan_catalog (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    plan_code VARCHAR(40) NOT NULL UNIQUE,
    display_name VARCHAR(120) NOT NULL,
    description VARCHAR(500) NULL,
    audience VARCHAR(200) NULL,
    amount_cents BIGINT NOT NULL,
    hybrid_addon_cents BIGINT NOT NULL DEFAULT 1000,
    trial_days INT NOT NULL DEFAULT 7,
    default_role_code VARCHAR(80) NOT NULL,
    per_seat BIT(1) NOT NULL DEFAULT 0,
    sort_order INT NOT NULL DEFAULT 0,
    is_active BIT(1) NOT NULL DEFAULT 1,
    features_json TEXT NULL,
    created_at DATETIME NULL,
    updated_at DATETIME NULL
);

-- User extras (ignore error if column already exists)
-- ALTER TABLE users ADD COLUMN hybrid_addon BIT(1) DEFAULT 0;
-- ALTER TABLE users ADD COLUMN subscription_seats INT DEFAULT 1;

-- If subscription_plan is MySQL ENUM, extend allowed values:
-- ALTER TABLE users MODIFY COLUMN subscription_plan VARCHAR(40) NULL;
-- (VARCHAR is safest for new plan codes)

INSERT INTO subscription_plan_catalog
(plan_code, display_name, description, audience, amount_cents, hybrid_addon_cents, trial_days, default_role_code, per_seat, sort_order, is_active, features_json, created_at, updated_at)
SELECT 'SOLOPRENEUR', 'Solopreneur (founder)', 'Freelancers, founders (50K base)', 'Freelancers / founders',
       1900, 1000, 7, 'NUMBRICS_BUSINESS_OWNER', 0, 1, 1,
       '["One-click scans","Basic IRS letter parsing","Growth tips","Multilingual"]', NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM subscription_plan_catalog WHERE plan_code = 'SOLOPRENEUR');

INSERT INTO subscription_plan_catalog
(plan_code, display_name, description, audience, amount_cents, hybrid_addon_cents, trial_days, default_role_code, per_seat, sort_order, is_active, features_json, created_at, updated_at)
SELECT 'BUSINESS_OWNER', 'Business Owner', 'SMB — e-comm & real estate owners', 'SMB owners',
       7900, 1000, 7, 'NUMBRICS_BUSINESS_OWNER', 0, 2, 1,
       '["Everything in Solopreneur","Team collab","Business Formation / Sales Tax","Advanced audit flags","Invoicing"]', NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM subscription_plan_catalog WHERE plan_code = 'BUSINESS_OWNER');

INSERT INTO subscription_plan_catalog
(plan_code, display_name, description, audience, amount_cents, hybrid_addon_cents, trial_days, default_role_code, per_seat, sort_order, is_active, features_json, created_at, updated_at)
SELECT 'ACCOUNTANT_PRO', 'Accountant Pro', 'For accountants managing multiple clients', 'Accountants / firms',
       19900, 1000, 7, 'NUMBRICS_ACCOUNTANT_PRO', 1, 3, 1,
       '["Everything in Business Owner","Bulk uploads","Memo drafting","Client intelligence","SOC 2 priority","White-labeling"]', NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM subscription_plan_catalog WHERE plan_code = 'ACCOUNTANT_PRO');
