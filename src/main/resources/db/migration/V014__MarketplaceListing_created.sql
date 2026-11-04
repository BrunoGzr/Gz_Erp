CREATE TABLE marketplace_listing(

    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY NOT NULL,
    public_id VARCHAR(36) UNIQUE NOT NULL,
    external_listing_id VARCHAR(256),
    title VARCHAR(256) NOT NULL,
    marketplace ENUM('SHOPEE','MERCADO_LIVRE') NOT NULL,
    stock INT UNSIGNED NOT NULL DEFAULT 0,
    sales INT UNSIGNED NOT NULL DEFAULT 0,
    sku VARCHAR(100) NOT NULL,
    marketplace_token_id BIGINT UNSIGNED NOT NULL,
    product_id_intern BIGINT UNSIGNED NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    UNIQUE (marketplace_token_id, external_listing_id),
    INDEX sku_marketplace_idx (sku , marketplace),
    CONSTRAINT fk_marketplace_token_id_marketplace_listings FOREIGN KEY (marketplace_token_id) REFERENCES marketplace_tokens(id) ON DELETE CASCADE,
    CONSTRAINT fk_product_marketplace_listings FOREIGN KEY (product_id_intern) REFERENCES products(id) ON DELETE CASCADE

);