-- Fix emoji / 4-byte UTF-8 storage for Taalr chat history.
-- Run once on production/staging MySQL.

ALTER TABLE taalr_chat_messages
    CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

ALTER TABLE taalr_chat_messages
    MODIFY content LONGTEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL;
