package com.erpapi.gzerp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class UserCreateDto {

    @NotNull
    private Long tenantId;
    @NotBlank
    @Size(min = 1, max = 32)
    private String userName;

    @NotBlank
    private String name;

    @NotBlank
    @Size(min = 6, max = 20)
    private String password;
    @Email
    @NotBlank
    @Size(min = 6, max = 254)
    private String email;

    public UserCreateDto() {
    }

    public UserCreateDto(String userName, String password, String email, Long tenantId, String name) {
        this.userName = userName;
        this.password = password;
        this.email = email;
        this.tenantId = tenantId;
        this.name = name;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
