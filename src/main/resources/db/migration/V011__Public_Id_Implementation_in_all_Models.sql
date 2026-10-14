ALTER TABLE tenants ADD COLUMN public_id CHAR(36) NULL AFTER id;

UPDATE tenants SET public_id = UUID() WHERE public_id IS NULL;

ALTER TABLE tenants
    MODIFY public_id CHAR(36) NOT NULL AFTER id,
    ADD CONSTRAINT uk_tenants_public_id UNIQUE (public_id);

ALTER TABLE users_accounts ADD COLUMN public_id CHAR(36) NULL AFTER id;

UPDATE users_accounts SET public_id = UUID() WHERE public_id IS NULL;

ALTER TABLE users_accounts
    MODIFY public_id CHAR(36) NOT NULL AFTER id,
    ADD CONSTRAINT uk_users_accounts_public_id UNIQUE (public_id);

ALTER TABLE marketplace_tokens ADD COLUMN public_id CHAR(36) NULL AFTER id;

UPDATE marketplace_tokens SET public_id = UUID() WHERE public_id IS NULL;

ALTER TABLE marketplace_tokens
    MODIFY public_id CHAR(36) NOT NULL AFTER id,
    ADD CONSTRAINT uk_marketplace_tokens_public_id UNIQUE (public_id);

