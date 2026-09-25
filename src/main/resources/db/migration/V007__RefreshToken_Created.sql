CREATE TABLE refresh_tokens
(
    id               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    token            CHAR(36)        NOT NULL,
    user_accounts_id BIGINT UNSIGNED NOT NULL,
    expires_at       DATETIME        NOT NULL,
    revoked          BOOL            NOT NULL DEFAULT FALSE,
    replaced_by      CHAR(36)        NULL,
    created_at       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_refresh_tokens_token (token),
    INDEX idx_refresh_tokens_user (user_accounts_id),
    INDEX idx_refresh_tokens_expires (expires_at),
    CONSTRAINT fk_refresh_tokens_user
        FOREIGN KEY (user_accounts_id) REFERENCES users_accounts(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

