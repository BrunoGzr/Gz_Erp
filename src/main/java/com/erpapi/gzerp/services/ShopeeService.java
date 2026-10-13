package com.erpapi.gzerp.services;

import com.erpapi.gzerp.enums.Marketplace;
import com.erpapi.gzerp.exceptions.*;
import com.erpapi.gzerp.integrations.shopee.RenewTokenRequestDto;
import com.erpapi.gzerp.integrations.shopee.RequestAuthBodyDto;
import com.erpapi.gzerp.integrations.shopee.RequestResponseAuthBodyDto;
import com.erpapi.gzerp.models.MarketplaceTokens;
import com.erpapi.gzerp.models.OauthIdentifier;
import com.erpapi.gzerp.repositories.MarketplacesTokensRepo;
import com.erpapi.gzerp.repositories.OauthIdentifierRepo;
import com.erpapi.gzerp.repositories.TenantsRepo;
import com.erpapi.gzerp.security.LocksPerKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.locks.Lock;

@Service
public class ShopeeService {

    private static final Logger log = LoggerFactory.getLogger(ShopeeService.class);
    private final OauthIdentifierRepo identifierRepo;
    private final RestClient restClient;
    private final TenantsRepo tenantsRepo;
    private final MarketplacesTokensRepo marketplacesTokensRepo;
    private final LocksPerKey lock;

    @Value("${SHOPEE_URL}")
    private String url;

    @Value("${SHOPEE_PARTNER_ID}")
    private Long partnerId;

    @Value("${SHOPEE_API_PARTNER_KEY}")
    private String partnerKey;

    @Value("${REDIRECT_URI}")
    private String redirectUri;

    @Value("${EXPIRE_SEC}")
    private Long expireSec;

    private final String apiSignPathFirstTime ="/api/v2/auth/token/get";

    @Value("${SHOPEE_API_HOST}")
    private String apiHost;

    @Value("${TEST_TENANT_ID_SHOPEE}")
    private Long testTenantId;

    private final String renewTokensPath = "/api/v2/auth/access_token/get";

    public ShopeeService(OauthIdentifierRepo identifierRepo, RestClient restClient, TenantsRepo tenantsRepo, MarketplacesTokensRepo marketplacesTokensRepo, LocksPerKey lock) {
        this.identifierRepo = identifierRepo;
        this.restClient = restClient;
        this.tenantsRepo = tenantsRepo;
        this.marketplacesTokensRepo = marketplacesTokensRepo;
        this.lock = lock;
    }

    public String authShopeeUriGen(Long tenantId) {

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

    @Transactional
    public void handleCallback(String code, Long shopId, String state) {

        Long tenantId;
        OauthIdentifier identifier = null;

        if (code == null) {
            throw new InvalidShopeeStateException("Invalid Null Code.");
        }

        if (shopId == null) {
            throw new InvalidShopeeStateException("Invalid Null shopId");
        }

        if (state != null && !state.isBlank()) {
//            Normal flux
            UUID stateUuid;
            try {
                stateUuid = UUID.fromString(state);
            } catch (IllegalArgumentException e) {
                throw new InvalidShopeeStateException("State malformed");
            }
            identifier = identifierRepo.findByState(stateUuid).orElseThrow(
                    () -> new InvalidShopeeStateException("State not found"));

            if (identifier.getExpiresAt().isBefore(LocalDateTime.now())) {
                throw new InvalidShopeeStateException("State invalid reason: Expired.");
            }
            tenantId = identifier.getTenantId();
        } else {
            log.warn("Callback without state, using fix tenantId for tests: {}", testTenantId);
            tenantId = testTenantId;
        }

        RequestResponseAuthBodyDto response = exchangeCodeRequest(new RequestAuthBodyDto(code, shopId, partnerId), getSign(apiSignPathFirstTime));
        if (response.error() != null && !response.error().isBlank()) {
            throw new ShopeeAuthResponseException(response.error());
        }
        MarketplaceTokens marketplace = marketplacesTokensRepo.findByMarketplaceAndShopId(Marketplace.SHOPEE, shopId)
                        .orElseGet(MarketplaceTokens::new);
        marketplace.setMarketplace(Marketplace.SHOPEE);
        LocalDateTime now = LocalDateTime.now();
        marketplace.setAccessTokenExpiresAt(now.plusSeconds(response.expireIn()));
        marketplace.setRefreshTokenExpiresAt(now.plusDays(30));
        marketplace.setAccessToken(response.accessToken());
        marketplace.setShopId(shopId);
        marketplace.setRefreshToken(response.refreshToken());
        marketplace.setTenant(tenantsRepo.findById(tenantId).orElseThrow(
                () -> new InvalidCredentialsException("Tenant with this Id not found")
        ));
        if (identifier != null
                && marketplace.getAccountName() == null
                && identifier.getAccountName() != null) {
            marketplace.setAccountName(identifier.getAccountName());
        }
        marketplacesTokensRepo.save(marketplace);
        if (identifier != null) {
            identifierRepo.delete(identifier);
        } else {
            log.warn("Test flow, no identifier to delete.");
        }

    }

    private SignData getSign(String path) {

        long timestamp = Instant.now().getEpochSecond();
        String baseString = partnerId + path + timestamp;

        return new SignData(hmacSha256(baseString), timestamp);

    }

    public record SignData(String sign, long timestamp) {
    }

    private String hmacSha256(String baseString) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");

            SecretKeySpec keySpec = new SecretKeySpec(
                    partnerKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");

            mac.init(keySpec);

            byte[] rawHmac = mac.doFinal(baseString.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(rawHmac);

        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new HmacSHA256GenerationException("Error in generate the Shopee Hmac : " + e.getMessage());
        }

    }

    private RequestResponseAuthBodyDto exchangeCodeRequest(RequestAuthBodyDto request, SignData data) {

        String value = data.sign;
        long timestamp = data.timestamp;

        try {

            return restClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
                            .host(apiHost)
                            .path(apiSignPathFirstTime)
                            .queryParam("sign", value)
                            .queryParam("timestamp", timestamp)
                            .queryParam("partner_id", partnerId)
                            .build())
                    .body(request)
                    .retrieve()
                    .body(RequestResponseAuthBodyDto.class);

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("Shopee reject the first token exchange: status={}, body={}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new ExchangeCodeForTokensShopeeException("Failed in exchange code for tokens: " + e.getResponseBodyAsString());
        } catch (ResourceAccessException e) {
            throw new ExchangeCodeForTokensShopeeException("Connection Failed with the shopee server: " + e.getMessage());
        }
    }

    private RequestResponseAuthBodyDto renewTokensRequest(RenewTokenRequestDto request, SignData data) {
        String sign = data.sign;
        long timestamp = data.timestamp;

        try {
            return restClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
                            .host(apiHost)
                            .path(renewTokensPath)
                            .queryParam("sign", sign)
                            .queryParam("partner_id", partnerId)
                            .queryParam("timestamp", timestamp)
                            .build())
                    .body(request)
                    .retrieve()
                    .body(RequestResponseAuthBodyDto.class);

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("Shopee reject token exchange: status={}, body{}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new ExchangeCodeForTokensShopeeException("Failed in renew access / refresh tokens : " + e.getResponseBodyAsString());

        } catch (ResourceAccessException e) {
            throw new ExchangeCodeForTokensShopeeException("Connection Failed with the shopee server: " + e.getMessage());
        }

    }


//    @Transactional(propagation = Propagation.REQUIRES_NEW)
//    public MarketplaceTokens resolveValidToken (MarketplaceTokens token){
//        if (token.getAccessTokenExpiresAt().isAfter(LocalDateTime.now())){
//            return token;
//        }
//        Lock padLock = lock.getLock(token.getId());
//        padLock.lock();
//
//        try {
//            MarketplaceTokens atualToken = marketplacesTokensRepo.findByTenantId()
//
//        }
//
//
//
//
//
//
//    }
    
    
}