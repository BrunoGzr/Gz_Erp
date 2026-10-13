CREATE TABLE marketplace_tokens(
    id BIGINT UNSIGNED NOT NULL UNIQUE  PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT UNSIGNED NOT NULL,
    marketplace ENUM('SHOPEE','MERCADOLIVRE') NOT NULL,
    shop_id BIGINT UNIQUE NULL,
    access_token VARCHAR(500) NULL,
    refresh_token VARCHAR(500) NULL,
    access_token_expires_at DATETIME NULL,
    refresh_token_expires_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_marketplace_tokens_tenants FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    INDEX idx_access_expires_at (access_token_expires_at)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE oauth_identifier(
    id VARCHAR(36) NOT NULL UNIQUE PRIMARY KEY,
    tenant_id BIGINT UNSIGNED NOT NULL UNIQUE,
    marketplace ENUM('SHOPEE', 'MERCADOLIVRE') NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at DATETIME NOT NULL
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

