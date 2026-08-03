package com.erpapi.gzerp.dto;

public class LoginResponseDto {

    private String token;
    private String type;
    private String email;
    private Long tenantId;
    private boolean isAdmin;

    public LoginResponseDto() {
        this.type = "Bearer";
    }

    public LoginResponseDto(String token, String email, Long tenantId, boolean isAdmin) {
        this.token = token;
        this.type = "Bearer";
        this.email = email;
        this.tenantId = tenantId;
        this.isAdmin = isAdmin;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }
}
