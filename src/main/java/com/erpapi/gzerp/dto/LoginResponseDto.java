package com.erpapi.gzerp.dto;

import com.erpapi.gzerp.enums.UserType;

import java.util.UUID;

public class LoginResponseDto {

    private String token;
    private String type;
    private Long expirationInSec;
    private String refreshToken;
    private UUID userPublicId;
    private Long tenantId;
    private String userType;

    public LoginResponseDto() {
        this.type = "Bearer";
    }

    public LoginResponseDto(String token, Long expirationInSec, String refreshToken, UUID userId, Long tenantId, UserType userType) {
        this.token = token;
        this.type = "Bearer";
        this.expirationInSec = expirationInSec;
        this.refreshToken = refreshToken;
        this.userPublicId = userId;
        this.tenantId = tenantId;
        this.userType = userType.name();
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getExpirationIn() {
        return expirationInSec;
    }

    public void setExpirationIn(Long expirationIn) {
        this.expirationInSec = expirationIn;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public UUID getUserPublicId() {
        return userPublicId;
    }

    public void setUserPublicId(UUID userPublicId) {
        this.userPublicId = userPublicId;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }
}
