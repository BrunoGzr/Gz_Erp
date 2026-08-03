package com.erpapi.gzerp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class LoginRequestDto {

    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Size(min = 6, max = 256)
    private String password;

    @NotNull
    private Long tenantId;

    public LoginRequestDto() {
    }

    public LoginRequestDto(String email, String password, Long tenantId) {
        this.email = email;
        this.password = password;
        this.tenantId = tenantId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }
}
