package com.erpapi.gzerp.repositories;

import com.erpapi.gzerp.enums.Marketplace;
import com.erpapi.gzerp.models.MarketplaceTokens;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MarketplacesTokensRepo extends JpaRepository<MarketplaceTokens, Long> {

    Optional<MarketplaceTokens> findByTenantId(Long tenantId);

    Optional<MarketplaceTokens> findByMarketplaceAndShopId(Marketplace marketplace, Long shopId);
}
