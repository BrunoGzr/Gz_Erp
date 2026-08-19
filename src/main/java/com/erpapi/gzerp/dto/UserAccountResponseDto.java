package com.erpapi.gzerp.dto;

import com.erpapi.gzerp.models.UsersAccounts;

public class UserAccountResponseDto {

    private String email;

    private String username;

    private Long tenantId;

    public UserAccountResponseDto() {
    }

    public UserAccountResponseDto(UsersAccounts user) {
        this.email = user.getEmail();
        this.username = user.getUsername();
        this.tenantId = user.getTenantId();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }
}
