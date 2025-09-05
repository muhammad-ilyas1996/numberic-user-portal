USE medical_billing_ehr;

-- Insert parent menus with correct column names
INSERT INTO menus (menu_name, display_name, menu_url, menu_order, parent_menu_id, portal_type_id, icon_class, CREATED_ON, MODIFIED_ON, CREATED_BY, MODIFIED_BY, IS_ACTIVE)
VALUES
('HOME', 'Home', '/home', 1, NULL, 2, 'fas fa-home', NOW(), NOW(), 'system', 'system', true),
('EHR', 'EHR', '/ehr', 2, NULL, 2, 'fas fa-file-medical', NOW(), NOW(), 'system', 'system', true),
('PATIENTS', 'Patients', '/patients', 3, NULL, 2, 'fas fa-users', NOW(), NOW(), 'system', 'system', true),
('SCHEDULE', 'Schedule', '/schedule', 4, NULL, 2, 'fas fa-calendar', NOW(), NOW(), 'system', 'system', true),
('BILLING', 'Billing', '/billing', 5, NULL, 2, 'fas fa-file-invoice-dollar', NOW(), NOW(), 'system', 'system', true),
('REAL-TIME', 'Real-Time', '/realtime', 6, NULL, 2, 'fas fa-clock', NOW(), NOW(), 'system', 'system', true),
('REPORTS', 'Reports', '/reports', 7, NULL, 2, 'fas fa-chart-bar', NOW(), NOW(), 'system', 'system', true),
('ICON', 'Icon', '/icon', 8, NULL, 2, 'fas fa-icons', NOW(), NOW(), 'system', 'system', true),
('TASKS & MESSAGES', 'Tasks & Messages', '/tasks', 9, NULL, 2, 'fas fa-tasks', NOW(), NOW(), 'system', 'system', true),
('ADMIN', 'Admin', '/admin', 10, NULL, 2, 'fas fa-user-cog', NOW(), NOW(), 'system', 'system', true);


-- Get BILLING menu ID
SELECT menu_id FROM menus WHERE menu_name = 'BILLING' AND portal_type_id = 2;

-- Add BILLING submenus (replace 5 with actual BILLING menu_id)
INSERT INTO menus (menu_name, display_name, menu_url, menu_order, parent_menu_id, portal_type_id, icon_class, CREATED_ON, MODIFIED_ON, CREATED_BY, MODIFIED_BY, IS_ACTIVE)
VALUES
('ENTER CHARGES', 'Enter Charges', '/billing/enter-charges', 1, 5, 2, 'fas fa-plus', NOW(), NOW(), 'system', 'system', true),
('CLAIMS', 'Claims', '/billing/claims', 2, 5, 2, 'fas fa-file-alt', NOW(), NOW(), 'system', 'system', true),
('PAYMENTS', 'Payments', '/billing/payments', 3, 5, 2, 'fas fa-credit-card', NOW(), NOW(), 'system', 'system', true),
('ELECTRONIC REMITTANCE ADVICE (ERA)', 'Electronic Remittance Advice (ERA)', '/billing/era', 4, 5, 2, 'fas fa-file-invoice', NOW(), NOW(), 'system', 'system', true),
('A/R LEDGERS', 'A/R Ledgers', '/billing/ar-ledgers', 5, 5, 2, 'fas fa-book', NOW(), NOW(), 'system', 'system', true),
('SECONDARY BILLING', 'Secondary Billing', '/billing/secondary', 6, 5, 2, 'fas fa-file-invoice-dollar', NOW(), NOW(), 'system', 'system', true),
('STATEMENTS', 'Statements', '/billing/statements', 7, 5, 2, 'fas fa-file-pdf', NOW(), NOW(), 'system', 'system', true),
('COLLECTIONS', 'Collections', '/billing/collections', 8, 5, 2, 'fas fa-hand-holding-usd', NOW(), NOW(), 'system', 'system', true);



USE medical_billing_ehr;

-- Insert all permissions from client's PDF with correct column names
INSERT INTO permissions (code_name, display_name, category, description, portal_type_id, is_superadmin, CREATED_ON, MODIFIED_ON, CREATED_BY, MODIFIED_BY, IS_ACTIVE)
VALUES
-- General Permissions
('ADMINISTRATION_RIGHTS', 'Administration Rights', 'GENERAL', 'Grants access to the Admin module, allowing management of users, roles, practice settings, and system configurations', 2, false, NOW(), NOW(), 'system', 'system', true),
('ALL', 'All Permissions', 'GENERAL', 'Provides full access to every module and feature across the platform. Equivalent to super admin rights', 2, true, NOW(), NOW(), 'system', 'system', true),

-- Billing Permissions
('ELIGIBILITY', 'Eligibility', 'BILLING', 'Allows users to verify patient insurance eligibility before scheduling or billing claims', 2, false, NOW(), NOW(), 'system', 'system', true),
('PRECERT_INQUIRY', 'Precert Inquiry', 'BILLING', 'Grants the ability to view existing precertification (prior authorization) requests', 2, false, NOW(), NOW(), 'system', 'system', true),
('PRECERT_ADD', 'Precert Add', 'BILLING', 'Grants the ability to create and submit new precertification requests for insurance approval', 2, false, NOW(), NOW(), 'system', 'system', true),
('CLAIM_STATUS_INQUIRY', 'Claim Status Inquiry', 'BILLING', 'Enables inquiry into the current status of submitted claims', 2, false, NOW(), NOW(), 'system', 'system', true),
('CLAIM_UPLOAD', 'Claim Upload', 'BILLING', 'Allows bulk uploading of claims (usually via EDI/X12 format) to insurance payers', 2, false, NOW(), NOW(), 'system', 'system', true),
('CLAIM_DIRECT_DATA_ENTRY', 'Claim Direct Data Entry', 'BILLING', 'Grants ability to manually enter claim details directly into the system', 2, false, NOW(), NOW(), 'system', 'system', true),
('CLAIM_STATUS_DISPLAY', 'Claim Status Display', 'BILLING', 'Allows viewing the full history and detailed status updates of claims', 2, false, NOW(), NOW(), 'system', 'system', true),
('ERA', 'Electronic Remittance Advice', 'BILLING', 'Provides access to electronic remittance/payment information from insurance companies', 2, false, NOW(), NOW(), 'system', 'system', true),
('CHARGE_ENTRY', 'Charge Entry', 'BILLING', 'Grants the ability to enter charges for services provided (CPT/HCPCS codes, modifiers, units)', 2, false, NOW(), NOW(), 'system', 'system', true),
('RESTRICT_FINANCIAL_INFORMATION', 'Restrict Financial Information', 'BILLING', 'Restricts user from viewing sensitive financial data such as balances, collections, payer reimbursements', 2, false, NOW(), NOW(), 'system', 'system', true),

-- Patient/EHR Permissions
('REFERRAL_INQUIRY', 'Referral Inquiry', 'PATIENT', 'Allows users to check existing patient referrals (e.g., referral to a specialist)', 2, false, NOW(), NOW(), 'system', 'system', true),
('REFERRAL_ADD', 'Referral Add', 'PATIENT', 'Grants ability to add new patient referrals into the system', 2, false, NOW(), NOW(), 'system', 'system', true),
('EHR_ADMIN', 'EHR Admin', 'EHR', 'Grants access to EHR administrative settings (templates, chart notes setup, workflows, provider settings)', 2, false, NOW(), NOW(), 'system', 'system', true),

-- Additional Permissions
('EDIT_ADMIN', 'Edit Admin', 'GENERAL', 'Allows editing of administrative settings', 2, false, NOW(), NOW(), 'system', 'system', true),
('SELF_ASSIGN', 'Self Assign', 'GENERAL', 'Allows user to self-assign tasks and responsibilities', 2, false, NOW(), NOW(), 'system', 'system', true);





ALTER TABLE roles
    MODIFY COLUMN description TEXT;



INSERT INTO roles (code_name, display_name, description, portal_type_id, is_superadmin, is_readonly, CREATED_ON, MODIFIED_ON, CREATED_BY, MODIFIED_BY, IS_ACTIVE)
VALUES
('PM_SUPER_ADMIN', 'PM Super Administrator', 'PM Portal Super Administrator - Full access to all features', 2, true, false, NOW(), NOW(), 'system', 'system', true),
('PM_ADMIN', 'PM Administrator', 'PM Portal Administrator - Manage users and settings', 2, false, false, NOW(), NOW(), 'system', 'system', true),
('PM_MANAGER', 'PM Manager', 'PM Portal Manager - Manage patients and billing', 2, false, false, NOW(), NOW(), 'system', 'system', true),
('PM_USER', 'PM User', 'PM Portal User - Basic access to assigned features', 2, false, true, NOW(), NOW(), 'system', 'system', true);


select * from role_permissions rp ;

-- Delete extra audit columns
ALTER TABLE role_permissions
DROP COLUMN added_by,
DROP COLUMN added_at,
DROP COLUMN created_at,
DROP COLUMN created_by,
DROP COLUMN is_active,
DROP COLUMN updated_at,
DROP COLUMN updated_by;



INSERT INTO role_permissions (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM roles r, permissions p
WHERE r.code_name = 'PM_SUPER_ADMIN' AND p.portal_type_id = 2;

-- PM_ADMIN gets most permissions (except super admin ones)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM roles r, permissions p
WHERE r.code_name = 'PM_ADMIN' AND p.portal_type_id = 2
  AND p.code_name NOT IN ('ALL', 'RESTRICT_FINANCIAL_INFORMATION');

-- PM_MANAGER gets patient and billing management permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM roles r, permissions p
WHERE r.code_name = 'PM_MANAGER' AND p.portal_type_id = 2
  AND p.code_name IN ('ELIGIBILITY', 'PRECERT_INQUIRY', 'PRECERT_ADD', 'REFERRAL_INQUIRY', 'REFERRAL_ADD', 'CLAIM_STATUS_INQUIRY', 'CLAIM_UPLOAD', 'CLAIM_DIRECT_DATA_ENTRY', 'CLAIM_STATUS_DISPLAY', 'ERA', 'CHARGE_ENTRY');

-- PM_USER gets basic view permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM roles r, permissions p
WHERE r.code_name = 'PM_USER' AND p.portal_type_id = 2
  AND p.code_name IN ('ELIGIBILITY', 'PRECERT_INQUIRY', 'REFERRAL_INQUIRY', 'CLAIM_STATUS_INQUIRY', 'CLAIM_STATUS_DISPLAY');



describe user_roles ;

-- Remove extra columns from user_roles table
ALTER TABLE user_roles
DROP COLUMN created_on;
DROP COLUMN created_by,
DROP COLUMN is_active,
DROP COLUMN updated_at,
DROP COLUMN updated_by;


INSERT INTO user_roles (user_id, role_id, CREATED_ON, MODIFIED_ON, CREATED_BY, MODIFIED_BY, IS_ACTIVE)
SELECT u.user_id, r.role_id, NOW(), NOW(), 'system', 'system', true
FROM users u, roles r
WHERE u.username = 'ali_ahmed_015' AND r.code_name = 'PM_SUPER_ADMIN';


-- Increase description column size to TEXT
ALTER TABLE roles
    MODIFY COLUMN description TEXT;



UPDATE users
SET portal_type_id = 1
WHERE portal_type_id =0
  AND portal_type_id NOT IN (SELECT portal_type_id FROM portal_types);



-- Insert Menu-Permission Mappings
INSERT INTO menu_permissions (menu_id, permission_id) VALUES
-- HOME Menu (All users can see home)
(1, 1056),

-- EHR Menu (EHR Admin permission)
(2, 1069),

-- PATIENTS Menu (Patient related permissions)
(3, 1057),
(3, 1067),
(3, 1068),
-- SCHEDULE Menu (Basic access)
(4, 1056), -- ALL
-- BILLING Menu (All billing permissions)
(5, 1057),
(5, 1058),
(5, 1059),
(5, 1060),
(5, 1061),
(5, 1062),
(5, 1063),
(5, 1064),
(5, 1065),
(5, 1066),
-- REAL-TIME Menu (Billing related)
(6, 1060),
(6, 1063),

-- REPORTS Menu (Admin and Financial)
(7, 1055),
(7, 1066),

-- ICON Menu (Basic access)
(8, 1056),
-- TASKS & MESSAGES Menu (Self assign permission)
(9, 1071),
-- ADMIN Menu (Administration rights)
(10, 1055),
(10, 1070),
-- BILLING Submenus
-- ENTER CHARGES (Charge entry permission)
(11, 1065),
(12, 1060),
(12, 1061),
(12, 1062),
(12, 1063),
-- PAYMENTS (Financial permissions)
(13, 1066),
-- ERA (ERA permission)
(14, 1064),
-- A/R LEDGERS (Financial permissions)
(15, 1066),
(16, 1061),
(16, 1062),
(17, 1066),
(18, 1066);
