package com.erpapi.gzerp.dto;

import com.erpapi.gzerp.models.Tenants;
import com.erpapi.gzerp.models.UsersAccounts;

public class UsersAccountsResponseDto {

    private String email;

    private String username;

    private Tenants tenant;

    public UsersAccountsResponseDto() {
    }

    public UsersAccountsResponseDto(UsersAccounts user) {
        this.email = user.getEmail();
        this.username = user.getUsername();
        this.tenant = user.getTenant();
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

    public Tenants getTenant() {
        return tenant;
    }

    public void setTenant(Tenants tenant) {
        this.tenant = tenant;
    }
}
