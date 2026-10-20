package com.erpapi.gzerp.integrations.shopee.dto;

public record IsTokenExpiredDto(
        Boolean isExpired,
        String accessToken,
        String refreshToken,
        Long shopId
) {}
