package com.erpapi.gzerp.dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class UsersAccountRegisterDto {

    @Email
    @NotBlank
    @Size(max = 256)
    private String email;

    @Size(max = 50)
    @NotBlank
    private String username;

    @NotBlank
    @Size(max = 256)
    private String password;

    @NotBlank
    private Long tenantId;

    @NotBlank
    @Size(min = 10, max = 20)
    private String cpf;

    @NotBlank
    @Size(min = 10, max = 20)
    private String phone;

    @Nullable
    private BigDecimal ownership;

    @NotBlank(message = "Name is required")
    @Size(min = 3 , max = 256)
    private String fullName;

    private BigDecimal salary;

    public UsersAccountRegisterDto() {
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

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Nullable
    public BigDecimal getOwnership() {
        return ownership;
    }

    public void setOwnership(@Nullable BigDecimal ownership) {
        this.ownership = ownership;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public BigDecimal getSalary() {
        return salary;
    }

    public void setSalary(BigDecimal salary) {
        this.salary = salary;
    }
}
