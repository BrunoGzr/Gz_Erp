package com.erpapi.gzerp.repositories;

import com.erpapi.gzerp.dto.Products.MarketplaceListing.ListingCountProjectionDto;
import com.erpapi.gzerp.models.MarketplaceListing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface MarketplaceListingRepo extends JpaRepository<MarketplaceListing, Long> {


    @Query("""
    SELECT new map(l.product.id as productId, l.marketplace as marketplace, count(l) as total)
    FROM MarketplaceListing l
    WHERE l.product.id  IN :productIds
    AND l.active = true
    GROUP BY l.product.id, l.marketplace
""")
    List<ListingCountProjectionDto> countListingsByProductIds(@Param("productIds") List<Long> productIds);

    List<MarketplaceListing> findAllByProduct_publicIdAndActiveTrue(UUID publicId);


    @Modifying
    @Query("""
    UPDATE MarketplaceListing ml
        SET ml.active = false
            WHERE ml.product.publicId = :publicId
                AND ml.active = true
    """)
    int deactivateByProductPublicid(UUID publicId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
    UPDATE MarketplaceListing ml
        SET ml.active = false 
            WHERE ml.product.publicId IN :list
                AND ml.active = true""")
    int deactivateByProductPublicIdInList(List<UUID> list);
}
