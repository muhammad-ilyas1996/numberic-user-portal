-- Taalr structured onboarding + optional full JSON snapshot (run if not using JPA ddl-auto=update).

ALTER TABLE business_profiles
    ADD COLUMN onboarding_track          VARCHAR(64)   NULL,
    ADD COLUMN filing_status             VARCHAR(64)   NULL,
    ADD COLUMN income_source_codes       VARCHAR(512)  NULL,
    ADD COLUMN pain_point_code           VARCHAR(32)   NULL,
    ADD COLUMN operations_codes          VARCHAR(512)  NULL,
    ADD COLUMN tax_pro_relationship      VARCHAR(64)   NULL,
    ADD COLUMN business_tier_choice      VARCHAR(64)   NULL,
    ADD COLUMN tax_pro_credential        VARCHAR(64)   NULL,
    ADD COLUMN practice_client_band      VARCHAR(64)   NULL,
    ADD COLUMN practice_tax_software     VARCHAR(128)  NULL,
    ADD COLUMN practice_pain_codes       VARCHAR(512)  NULL,
    ADD COLUMN onboarding_answers_json   LONGTEXT      NULL;
