package com.erpapi.gzerp.integrations.shopee;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RequestResponseAuthBody(
        @JsonProperty("request_id") String requestId,
        @JsonProperty("error") String error,
        @JsonProperty("refresh_token") String refreshToken,
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("expire_in") int expireIn,
        @JsonProperty("message")  String message,
        @JsonProperty("merchant_id_list")  long[] merchantIdList,
        @JsonProperty("shop_id_list")  long[] shopIdList,
        @JsonProperty("supplier_id_list")  long[] supplierIdList,
        @JsonProperty("user_id_list")  long[] userIdList
){}
