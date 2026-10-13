package com.erpapi.gzerp.integrations.shopee;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RenewTokenRequestDto(
        @JsonProperty("refresh_token") String refreshToken,
        @JsonProperty("partner_id") int partnerId,
        @JsonProperty("shop_id") int shopId,
        @JsonProperty("merchant_id") int merchantId
) {}
