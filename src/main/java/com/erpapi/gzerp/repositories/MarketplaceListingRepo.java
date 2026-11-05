package com.erpapi.gzerp.repositories;

import com.erpapi.gzerp.dto.ListingCountProjectionDto;
import com.erpapi.gzerp.models.MarketplaceListing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;

public interface MarketplaceListingRepo extends JpaRepository<MarketplaceListing, Long> {


    @Query("""
    SELECT new map(l.product.id as productId, l.marketplace as marketplace, count(l) as total)
    FROM MarketplaceListing l
    WHERE l.product.id IN :productIds
    GROUP BY l.product.id, l.marketplace
""")
    List<ListingCountProjectionDto> countListingsByProductIds(@Param("productIds") List<Long> productIds);

}
