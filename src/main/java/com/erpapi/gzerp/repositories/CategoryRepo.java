package com.erpapi.gzerp.repositories;

import com.erpapi.gzerp.dto.CategoryByProductProjection;
import com.erpapi.gzerp.models.Categories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepo extends JpaRepository<Categories, Long> {

    Optional<Categories> findByNameAndTenant_Id(String name, Long tenantId);

    Boolean existsByNameAndTenant_Id(String name, Long tenantId);

    @Query("""
        SELECT c.id FROM Categories c
        WHERE c.publicId IN :publicIds
          AND c.tenant.id = :tenantId
    """)
    List<Long> findIdsByPublicIdsAndTenantId(
            @Param("publicIds") List<UUID> publicIds,
            @Param("tenantId") Long tenantId);



    @Query("""
        SELECT p.id         AS productId,
               c.publicId   AS publicId,
               c.name       AS name
        FROM Products p
        JOIN p.categories c
        WHERE p.id IN :productIds
    """)
    List<CategoryByProductProjection> findCategoriesByProductIds(
            @Param("productIds") List<Long> productIds);


    List<Categories> findAllByActiveTrueAndTenant_IdOrderByNameAsc(Long tenantId);

    List<Categories> findAllByActiveTrueAndTenant_IdAndPublicIdIn(Long tenantId,List<UUID> publicId);

}
