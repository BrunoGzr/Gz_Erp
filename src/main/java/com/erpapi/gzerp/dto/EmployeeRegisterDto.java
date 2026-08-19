package com.erpapi.gzerp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class EmployeeRegisterDto {

    @NotNull
    private Long tenantId;
    @NotBlank
    @Size(min = 1, max = 32)
    private String userName;

    @NotBlank
    private String fullName;

    @NotBlank
    @Size(min = 6, max = 20)
    private String password;
    @Email
    @NotBlank
    @Size(min = 6, max = 254)
    private String email;

    @NotBlank
    private BigDecimal salary;

    @NotBlank
    @Size(min = 11, max = 14)
    private String cpf;

    @NotBlank
    @Size(min = 11, max = 15)
    private String phone;

    public EmployeeRegisterDto(UserAccountRegisterDto dto) {
        this.email = dto.getEmail();
        this.userName = dto.getUsername();
        this.password = dto.getPassword();
        this.tenantId = dto.getTenantId();
        this.cpf = dto.getCpf();
        this.phone = dto.getPhone();
        this.fullName = dto.getFullName();
    }

    public EmployeeRegisterDto() {

    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public BigDecimal getSalary() {
        return salary;
    }

    public void setSalary(BigDecimal salary) {
        this.salary = salary;
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

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}
