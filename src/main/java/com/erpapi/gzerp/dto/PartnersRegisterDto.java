package com.erpapi.gzerp.dto;

import com.erpapi.gzerp.models.Tenants;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class PartnersRegisterDto {

    @NotNull
    @Size(min = 10, max = 20)
    private String cpf;

    @NotEmpty
    private String fullName;

    @NotEmpty
    @Email
    private String email;

    @NotEmpty
    private String phone;

    @Nullable
    private BigDecimal ownership;

    @Nullable
    private Float salary;


    public PartnersRegisterDto() {};

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
    public Float getSalary() {
        return salary;
    }

    public void setSalary(@Nullable Float salary) {
        this.salary = salary;
    }
}
