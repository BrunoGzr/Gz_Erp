ALTER TABLE categories
    ADD COLUMN category_type ENUM('PRODUCT', 'STATE') NULL;

ALTER TABLE default_categories
    ADD COLUMN category_type ENUM('PRODUCT','STATE') NULL;

UPDATE default_categories
SET category_type = 'STATE'
WHERE name IN ('Baixo Giro', 'Promoções', 'Kit e Combos');

UPDATE default_categories
SET category_type = 'PRODUCT'
WHERE category_type IS NULL;

UPDATE categories c
    JOIN default_categories d ON d.name = c.name
SET c.category_type = d.category_type;

ALTER TABLE categories
    MODIFY category_type ENUM('PRODUCT','STATE')
        NOT NULL DEFAULT 'PRODUCT';

ALTER TABLE default_categories
    MODIFY category_type ENUM('PRODUCT','STATE')
        NOT NULL DEFAULT 'PRODUCT';

CREATE INDEX idx_categories_tenant_type_active
    ON categories (tenant_id, category_type, active);
