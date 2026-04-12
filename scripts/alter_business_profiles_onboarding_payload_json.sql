-- Full POST body snapshot (JSON). Run if not using JPA ddl-auto=update.

ALTER TABLE business_profiles
    ADD COLUMN onboarding_payload_json LONGTEXT NULL;
