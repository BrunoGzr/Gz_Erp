package com.erpapi.gzerp.services;

import com.erpapi.gzerp.enums.Marketplace;
import com.erpapi.gzerp.exceptions.ExchangeCodeForTokensShopeeException;
import com.erpapi.gzerp.exceptions.HmacSHA265GenerationException;
import com.erpapi.gzerp.exceptions.ShopeeAuthResponseException;
import com.erpapi.gzerp.integrations.shopee.RequestAuthBody;
import com.erpapi.gzerp.integrations.shopee.RequestResponseAuthBody;
import com.erpapi.gzerp.models.MarketplaceTokens;
import com.erpapi.gzerp.models.OauthIdentifier;
import com.erpapi.gzerp.repositories.OauthIdentifierRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ShopeeService {

    private static final Logger log = LoggerFactory.getLogger(ShopeeService.class);
    private final OauthIdentifierRepo identifierRepo;
    private final RestClient restClient;

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

    @Value("${API_SIGN_PATH}")
    private String apiSignPath;

    public ShopeeService(OauthIdentifierRepo identifierRepo, RestClient restClient) {
        this.identifierRepo = identifierRepo;
        this.restClient = restClient;
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

        OauthIdentifier identifier = identifierRepo.findByState(UUID.fromString(state)).orElseThrow();;
        RequestResponseAuthBody response = exchangeCode(new RequestAuthBody(code,shop_id,partnerId),getSign());

        if (response.error() == null ||response.error().isBlank() ){
            throw new ShopeeAuthResponseException(response.error());
        }

        MarketplaceTokens marketplace = new MarketplaceTokens();






    }

    private SignData getSign(){

        long timestamp = Instant.now().getEpochSecond();
        String baseString = partnerId + apiSignPath + timestamp ;

        return new SignData(hmacSha256(baseString),timestamp);



    }


    public record SignData(String sign, long timestamp) {}

    private String hmacSha256(String baseString){
        try {
            Mac mac = Mac.getInstance("HmacSHA256");

            SecretKeySpec keySpec = new SecretKeySpec(
                    partnerKey.getBytes(StandardCharsets.UTF_8),"HmacSHA256");

            mac.init(keySpec);

            byte[] rawHmac = mac.doFinal(baseString.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(rawHmac);

        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new HmacSHA265GenerationException("Error in generate the Shopee Hmac : " + e.getMessage());
        }

    }

    private RequestResponseAuthBody exchangeCode(RequestAuthBody request, SignData data) {

        String value = data.sign;
        long timestamp = data.timestamp;


        try {


            return restClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
                            .host("openplatform.sandbox.test-stable.shopee.sg")
                            .path("/api/v2/auth/token/get")
                            .queryParam("sign", value)
                            .queryParam("timestamp", timestamp)
                            .queryParam("partner_id", partnerId)
                            .build())
                    .body(request)
                    .retrieve()
                    .body(RequestResponseAuthBody.class);

        }catch (HttpClientErrorException | HttpServerErrorException e){
            log.error("Shopee reject the token exchange: status={}, body={}",
                    e.getStatusCode(),e.getResponseBodyAsString());
            throw new ExchangeCodeForTokensShopeeException("Failed in exchange code for tokens" + e.getResponseBodyAsString());
        } catch (ResourceAccessException e){
            throw new ExchangeCodeForTokensShopeeException("Connection Failed with the shopee server" + e.getMessage());
        }
    }







}
