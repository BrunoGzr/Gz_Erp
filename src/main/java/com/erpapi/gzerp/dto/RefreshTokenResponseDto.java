package com.erpapi.gzerp.dto;

import com.erpapi.gzerp.models.RefreshToken;

public class RefreshTokenResponseDto {

    private final String refreshedToken;
    private final String token;
    private final String tokenType;
    private final long expiresIn;

    public RefreshTokenResponseDto(String refreshedToken, String token, String tokenType, long expiresIn) {
        this.refreshedToken = refreshedToken;
        this.token = token;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
    }

    public String getRefreshedToken() {
        return refreshedToken;
    }

    public String getToken() {
        return token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public long getExpiresIn() {
        return expiresIn;
    }
}
