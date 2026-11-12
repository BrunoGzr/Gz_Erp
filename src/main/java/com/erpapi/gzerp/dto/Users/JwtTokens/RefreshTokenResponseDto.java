package com.erpapi.gzerp.dto.Users.JwtTokens;

public class RefreshTokenResponseDto {

    private final String accessToken;
    private final long expiresInSec;

    public RefreshTokenResponseDto( String token, long expiresInDays) {

        this.accessToken = token;
        this.expiresInSec = expiresInDays;
    }

    public String getAccessToken() {
        return accessToken;
    }


    public long getExpiresInSec() {
        return expiresInSec;
    }
}
