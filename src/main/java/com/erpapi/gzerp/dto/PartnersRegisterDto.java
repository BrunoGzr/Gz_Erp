package com.erpapi.gzerp.dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class PartnersRegisterDto {

    @NotEmpty(message = "Please inform the Partner: CPF")
    @Size(min = 10, max = 20)
    private String cpf;

    @NotEmpty(message = "Please inform the Partner: Name")
    private String fullName;

    @Size(max = 50)
    @NotBlank(message = "Please inform the Partner: Username")
    private String username;

    @NotEmpty(message = "Please inform the Partner: Email")
    @Email
    private String email;

    @NotEmpty(message = "Please inform the Partner: Phone")
    private String phone;

    @Nullable
    private BigDecimal ownership;

    @Nullable
    private BigDecimal salary;

    private String password;

    public PartnersRegisterDto() {};

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    @Nullable
    public BigDecimal getSalary() {
        return salary;
    }

    public void setSalary(@Nullable BigDecimal salary) {
        this.salary = salary;
    }
}
