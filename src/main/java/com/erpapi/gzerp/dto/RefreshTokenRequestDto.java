package com.erpapi.gzerp.dto;

public class RefreshTokenRequestDto {

    private final String token;

    public RefreshTokenRequestDto(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }
}
