-- Taalr / Claude Managed Agents: one Anthropic session per user (optional if using JPA ddl-auto=update).

CREATE TABLE IF NOT EXISTS taalr_agent_sessions (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id                 BIGINT NOT NULL UNIQUE,
    anthropic_session_id    VARCHAR(128) NOT NULL,
    agent_id                VARCHAR(128) NULL,
    CREATED_BY              VARCHAR(255) NOT NULL,
    CREATED_ON              DATETIME NOT NULL,
    MODIFIED_BY             VARCHAR(255) NOT NULL,
    MODIFIED_ON             DATETIME NULL,
    IS_ACTIVE               TINYINT(1) NULL
);
