-- Taalr action sessions (receipt confirm, invoice draft). Optional if using JPA ddl-auto=update.

CREATE TABLE IF NOT EXISTS taalr_action_sessions (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT       NULL,
    phone_number    VARCHAR(32)  NULL,
    channel         VARCHAR(16)  NOT NULL,
    pending_action  VARCHAR(32)  NULL,
    context_json    LONGTEXT     NULL,
    expires_at      DATETIME     NOT NULL,
    CREATED_BY      VARCHAR(255) NOT NULL,
    CREATED_ON      DATETIME     NOT NULL,
    MODIFIED_BY     VARCHAR(255) NOT NULL,
    MODIFIED_ON     DATETIME     NULL,
    IS_ACTIVE       TINYINT(1)   NULL,
    INDEX idx_taalr_action_user (user_id),
    INDEX idx_taalr_action_phone (phone_number)
);
