package com.erpapi.gzerp.dto.Users.UsersAccounts;

import jakarta.validation.constraints.NotBlank;

public class LogoutRequestDto {
    @NotBlank
    private final String refreshToken;

    public LogoutRequestDto(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }
}
