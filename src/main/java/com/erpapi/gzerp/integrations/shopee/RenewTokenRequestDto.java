package com.erpapi.gzerp.integrations.shopee;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RenewTokenRequestDto(
        @JsonProperty("refresh_token") String refreshToken,
        @JsonProperty("partner_id") Long partnerId,
        @JsonProperty("shop_id") Long shopId
) {}
