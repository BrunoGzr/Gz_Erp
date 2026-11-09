package com.erpapi.gzerp.repositories;

import com.erpapi.gzerp.models.Products;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface ProductsRepo extends JpaRepository<Products, Long>, JpaSpecificationExecutor<Products> {

    boolean existsBySkuAndTenantId(String sku, Long tenantId);
    boolean existsByNameAndTenantId(String name, Long tenantId);

    boolean existsBySkuAndTenant_IdAndIdNot(String sku, Long tenantId, Long id);

    boolean existsByNameAndTenantIdAndIdNot(String name, Long tenantId, Long id);

    Page<Products> findByTenantIdAndActiveTrue(Long tenantId,
                                               Pageable pageable);

    Optional<Products> findByPublicIdAndTenant_IdAndActiveTrue(UUID publicId, Long tenant_id);





}
