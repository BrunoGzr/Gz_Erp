ALTER TABLE products ADD
    UNIQUE INDEX idx_name_tenant_id (tenant_id,name);

ALTER TABLE products ADD
    UNIQUE INDEX idx_tenant_id_sku (tenant_id,sku);

ALTER TABLE categories ADD
    UNIQUE INDEX idx_tenant_id_public_id (tenant_id,public_id);
