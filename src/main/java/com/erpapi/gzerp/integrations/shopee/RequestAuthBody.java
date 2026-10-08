package com.erpapi.gzerp.integrations.shopee;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RequestAuthBody(
        @JsonProperty("code")       String code,
        @JsonProperty("shop_id")    Long shopId,
        @JsonProperty("partner_id") Long partnerId
){}
