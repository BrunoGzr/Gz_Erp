package com.erpapi.gzerp.repositories;

import com.erpapi.gzerp.models.Products;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.*;

public interface ProductsRepo extends JpaRepository<Products, Long>, JpaSpecificationExecutor<Products> {

    boolean existsBySkuAndTenantId(String sku, Long tenantId);
    boolean existsByNameAndTenantId(String name, Long tenantId);

    boolean existsBySkuAndTenant_IdAndIdNot(String sku, Long tenantId, Long id);

    boolean existsByNameAndTenantIdAndIdNot(String name, Long tenantId, Long id);

    Page<Products> findByTenantIdAndActiveTrue(Long tenantId,
                                               Pageable pageable);

    Optional<Products> findByPublicIdAndTenant_IdAndActiveTrue(UUID publicId, Long tenantId);


    List<Products> findAllByTenant_IdAndActiveTrueAndPublicIdIn(Long tenantId, Collection<UUID> publicIds);



    @Modifying
    @Query("""
    UPDATE Products p 
        SET p.active = false 
            WHERE p.id IN :ids
                AND p.active = true
                    AND p.tenant.id = :tenantId """)
    void deactivateByTenant_IdAndActiveTrueAndIdIn(List<Long> ids, Long tenantId);



}
