CREATE TABLE categories
(
    id         BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    public_id  VARCHAR(36) UNIQUE NOT NULL,
    tenant_id  BIGINT UNSIGNED    NOT NULL,
    name       VARCHAR(100)       NOT NULL,
    active     BOOL               NOT NULL DEFAULT TRUE,
    created_at DATETIME           NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,


    INDEX idx_categories_name (name),
    UNIQUE KEY uk_name_tenant_id (tenant_id, name),
    CONSTRAINT fk_categories_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;;

CREATE TABLE default_categories
(
    id         BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(100) NOT NULL UNIQUE,
    active     BOOL         NOT NULL DEFAULT TRUE,
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_categories_name (name)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

INSERT INTO default_categories(name)
VALUES ('Eletrônicos'),
       ('Celulares e Acessórios'),
       ('Moda e Vestuário'),
       ('Casa e Decoração'),
       ('Beleza e Cuidados Pessoais'),
       ('Cozinha e Utilidades'),
       ('Ferramentas e Construção'),
       ('Brinquedos e Jogos'),
       ('Pet Shop'),
       ('Promoções'),
       ('Baixo Giro'),
       ('Kit e Combos');

CREATE TABLE product_categories (
    product_id BIGINT UNSIGNED NOT NULL,
    category_id BIGINT UNSIGNED NOT NULL,

    PRIMARY KEY (product_id,category_id),
    CONSTRAINT fk_pc_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT fk_pc_categories FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;