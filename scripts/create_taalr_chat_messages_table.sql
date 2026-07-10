-- Multi-turn history for Taalr when using Messages API fallback (anthropic.api.managed-agents-enabled=false).
-- Optional if using JPA ddl-auto=update.

CREATE TABLE IF NOT EXISTS taalr_chat_messages (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id      BIGINT       NOT NULL,
    role         VARCHAR(16)  NOT NULL,
    content      LONGTEXT     NOT NULL,
    CREATED_BY   VARCHAR(255) NOT NULL,
    CREATED_ON   DATETIME     NOT NULL,
    MODIFIED_BY  VARCHAR(255) NOT NULL,
    MODIFIED_ON  DATETIME     NULL,
    IS_ACTIVE    TINYINT(1)   NULL,
    INDEX idx_taalr_chat_user (user_id)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
