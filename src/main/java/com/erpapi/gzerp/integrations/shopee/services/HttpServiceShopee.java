package com.erpapi.gzerp.integrations.shopee.services;

import com.erpapi.gzerp.exceptions.ExchangeCodeForTokensShopeeException;
import com.erpapi.gzerp.integrations.shopee.dto.RenewTokenRequestDto;
import com.erpapi.gzerp.integrations.shopee.dto.RequestResponseAuthBodyDto;
import com.erpapi.gzerp.requisitions.HttpClientConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

@Service
public class HttpServiceShopee {

    private RestClient restClient;
    private static final Logger log = LoggerFactory.getLogger(ShopeeService.class);

    public HttpServiceShopee() {



    }


    public RequestResponseAuthBodyDto renewTokensRequest(RenewTokenRequestDto request, ShopeeService.SignData data
                                                ,String apiHost, String renewTokensPath, Long partnerId) {
        String sign = data.sign();
        long timestamp = data.timestamp();

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
}
