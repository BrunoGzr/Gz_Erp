package com.erpapi.gzerp.dto;

public class LoginResponseDto {

    private String token;
    private String type;
    private Long expirationIn;

    public LoginResponseDto() {
        this.type = "Bearer";
    }

    public LoginResponseDto(String token, Long expirationIn) {
        this.token = token;
        this.type = "Bearer";
        this.expirationIn = expirationIn;
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
        return expirationIn;
    }

    public void setExpirationIn(Long expirationIn) {
        this.expirationIn = expirationIn;
    }
}
