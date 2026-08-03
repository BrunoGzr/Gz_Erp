package com.erpapi.gzerp.models;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Entity
public class Partners {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenants tenant;

    @NotNull
    @Size(min = 10, max = 20)
    private String cpf;

    @NotNull
    @Size(min = 2, max = 256)
    private String fullName;

    @NotNull
    @Size(min = 5, max = 256)
    @Email
    private String email;

    @NotNull
    @Size(min = 10, max = 20)
    private String phone;

    @Column(precision = 5, scale = 2)
    @Nullable
    private BigDecimal ownership;

    @Nullable
    private BigDecimal salary;

    public Partners() {}

    public BigDecimal getSalary() {
        return salary;
    }

    public void setSalary(BigDecimal salary) {
        this.salary = salary;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Tenants getTenant() {
        return tenant;
    }

    public void setTenant(Tenants tenant) {
        this.tenant = tenant;
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
}
