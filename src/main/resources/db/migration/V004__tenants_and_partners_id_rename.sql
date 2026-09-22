CREATE TABLE users_accounts (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    user_type ENUM('PARTNER','EMPLOYEE','ADMIN') NOT NULL,
    tenant_id BIGINT UNSIGNED NOT NULL,
    FOREIGN KEY (tenant_id) REFERENCES tenants(id)
);

ALTER TABLE employees
RENAME COLUMN id TO user_account_id;

ALTER TABLE employees ADD FOREIGN KEY (user_account_id)
    REFERENCES users_accounts(id);

ALTER TABLE partners
RENAME COLUMN id TO user_account_id;

ALTER TABLE partners ADD FOREIGN KEY (user_account_id)
    REFERENCES users_accounts(id);

