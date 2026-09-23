CREATE TABLE roles(
    id BIGINT UNSIGNED PRIMARY KEY UNIQUE,
    tenant_id BIGINT UNSIGNED NULL,
    name VARCHAR(100) NOT NULL ,
    is_system BOOL DEFAULT false,
    description TEXT NULL,
    FOREIGN KEY (tenant_id) REFERENCES tenants(id),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_roles_tenant_name (tenant_id,name)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE role_permissions(
    role_id BIGINT UNSIGNED NOT NULL,
    permission VARCHAR(50) NOT NULL,
    PRIMARY KEY (role_id,permission),
    CONSTRAINT fk_role_permissions_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

ALTER TABLE users_accounts ADD COLUMN role_id BIGINT UNSIGNED NOT NULL DEFAULT 0;

INSERT INTO roles(id, tenant_id, name, is_system, description)
VALUES (1, null, 'DefaultRoleEmployees', true, 'Default role for new Employees Accounts');

INSERT INTO roles(id, tenant_id, name, is_system, description)
VALUES (2, null, 'DefaultRolePartners', true, 'Default role for new Partners Accounts');

INSERT INTO roles(id, tenant_id, name, is_system, description)
VALUES (3, null, 'DefaultRoleAdmin', true, 'Default role for new Admins Accounts');

ALTER TABLE roles AUTO_INCREMENT = 4 ;

ALTER TABLE users_accounts ADD CONSTRAINT fk_users_accounts_role
    FOREIGN KEY (role_id) REFERENCES roles(id);

