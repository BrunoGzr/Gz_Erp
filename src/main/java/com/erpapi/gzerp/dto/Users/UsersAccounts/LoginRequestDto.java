package com.erpapi.gzerp.dto.Users.UsersAccounts;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class LoginRequestDto {

    @NotBlank
    private String identifier;

    @NotBlank
    @Size(min = 6, max = 256)
    private String password;

    public LoginRequestDto() {
    }

    public LoginRequestDto(String identifier, String password) {
        this.identifier = identifier;
        this.password = password;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
