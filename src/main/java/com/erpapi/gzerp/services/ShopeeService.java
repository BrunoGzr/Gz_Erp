package com.erpapi.gzerp.services;

import com.erpapi.gzerp.enums.Marketplace;
import com.erpapi.gzerp.models.MarketplaceTokens;
import com.erpapi.gzerp.models.OauthIdentifier;
import com.erpapi.gzerp.repositories.OauthIdentifierRepo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ShopeeService {

    private final OauthIdentifierRepo identifierRepo;

    @Value("${SHOPEE_URL}")
    private String url;

    @Value("${SHOPEE_PARTNER_ID}")
    private String partnerId;

    @Value("${SHOPEE_API_PARTNER_KEY}")
    private String partnerKey;

    @Value("${REDIRECT_URI}")
    private String redirectUri;

    @Value("${EXPIRE_SEC}")
    private Long expireSec;

    public ShopeeService(OauthIdentifierRepo identifierRepo) {
        this.identifierRepo = identifierRepo;
    }

    public String authShopeeUriGen(Long tenantId){

        OauthIdentifier identifier = new OauthIdentifier();

        identifier.setState(UUID.randomUUID());
        identifier.setTenantId(tenantId);
        identifier.setCreatedAt(LocalDateTime.now());
        identifier.setExpiresAt(LocalDateTime.now().plusSeconds(expireSec));
        identifierRepo.save(identifier);

        return url
                + "?"
                + "partner_id="
                + partnerId
                + "&"
                + "auth_type=seller"
                + "&"
                + "redirect_uri="
                + redirectUri
                + "&"
                + "response_type=code"
                + "&"
                + "state="
                + identifier.getState().toString();


    }

    public Boolean handleCallback(String code, Long shop_id, String state){

        OauthIdentifier identifier = identifierRepo.findByState(UUID.fromString(state)).orElseThrow();

        MarketplaceTokens token = new MarketplaceTokens();

        token.setMarketplace(Marketplace.SHOPEE);
        token.setAccessToken();



    }







}
