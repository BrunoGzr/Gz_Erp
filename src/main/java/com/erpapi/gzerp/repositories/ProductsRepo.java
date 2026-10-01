package com.erpapi.gzerp.repositories;

import com.erpapi.gzerp.models.Products;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductsRepo extends JpaRepository<Products, Long> {

    boolean existsBySkuAndTenantId(String sku, Long tenantId);
    boolean existsByNameAndTenantId(String name, Long tenantId);

    Page<Products> findByTenantIdAndActiveTrue(Long tenantId,
                                               Pageable pageable);

}
