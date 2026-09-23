package com.erpapi.gzerp.models;

import com.erpapi.gzerp.dto.EmployeeRegisterDto;
import com.erpapi.gzerp.enums.UserType;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.bind.Name;


import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
public class Employees {

    @Id
    @Column(name = "user_account_id")
    private Long id;

    @NotNull
    private Long tenantId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_account_id")
    private UsersAccounts usersAccounts;

    @NotBlank
    @Size(min = 3, max = 256)
    @Column(name = "full_name")
    private String fullName;

    @NotBlank
    @Size(min = 0, max = 22)
    private String cpf;

    private Timestamp hireDate;

    @NotBlank
    private BigDecimal salary;

    private String phone;

    public Employees(EmployeeRegisterDto dto) {
        this.tenantId = dto.getTenantId();
        this.cpf = dto.getCpf();
        this.salary = dto.getSalary();

    }

    public Employees() {
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

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

    public Long getTenantid() {
        return tenantId;
    }

    public void setTenantid(Long tenantid) {
        this.tenantId = tenantid;
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

    public void setFullName(String name) {
        this.fullName = name;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public Timestamp getHireDate() {
        return hireDate;
    }

    public void setHireDate(Timestamp hireDate) {
        this.hireDate = hireDate;
    }

    public UsersAccounts getUserAccounts() {
        return usersAccounts;
    }

    public void setUsersAccounts(UsersAccounts usersAccounts) {
        this.usersAccounts = usersAccounts;
    }
}
