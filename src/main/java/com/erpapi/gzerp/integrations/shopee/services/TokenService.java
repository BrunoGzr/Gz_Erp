package com.erpapi.gzerp.integrations.shopee.services;

import com.erpapi.gzerp.enums.Marketplace;
import com.erpapi.gzerp.exceptions.ShopeeAuthResponseException;
import com.erpapi.gzerp.integrations.shopee.dto.RequestResponseAuthBodyDto;
import com.erpapi.gzerp.models.MarketplaceTokens;
import com.erpapi.gzerp.repositories.MarketplacesTokensRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class TokenService {

    private final MarketplacesTokensRepo marketplacesTokensRepo;

    public TokenService(MarketplacesTokensRepo marketplacesTokensRepo) {
        this.marketplacesTokensRepo = marketplacesTokensRepo;
    }

    @Transactional(readOnly = true)
    public MarketplaceTokens reValidateToken(Long shopId) {
        return marketplacesTokensRepo.findByMarketplaceAndShopId(Marketplace.SHOPEE, shopId)
                .orElseThrow(() -> new ShopeeAuthResponseException("Invalid Token to refresh"));
    }

    @Transactional
    public MarketplaceTokens saveToken(RequestResponseAuthBodyDto renewedToken, Long shopId){

        MarketplaceTokens token = marketplacesTokensRepo.findByMarketplaceAndShopId(Marketplace.SHOPEE, shopId).
                orElseThrow(() -> new ShopeeAuthResponseException("invalid Token to renew"));

        LocalDateTime now = LocalDateTime.now();
        token.setRefreshTokenExpiresAt(now.plusDays(30));
        token.setRefreshToken(renewedToken.refreshToken());
        token.setAccessTokenExpiresAt(now.plusSeconds(renewedToken.expireIn()));
        token.setAccessToken(renewedToken.accessToken());
        return marketplacesTokensRepo.save(token);

    }





}
