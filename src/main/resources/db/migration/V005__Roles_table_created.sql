CREATE TABLE roles(
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT UNIQUE,
    tenant_id BIGINT UNSIGNED UNIQUE NULL,
    name VARCHAR(100) NOT NULL UNIQUE ,
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