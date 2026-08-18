package com.erpapi.gzerp.models;

import com.erpapi.gzerp.enums.AccountType;
import com.erpapi.gzerp.enums.Roles;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
public class Employees {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private Long tenantId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_account_id")
    private UsersAccounts usersAccount;

    @Enumerated(EnumType.STRING)
    @NotBlank
    private AccountType accountType;

    @NotBlank
    @Size(min = 3, max = 20)
    private String userName;

    @NotBlank
    @Size(min = 3, max = 100)
    private String name;

    @Size(min = 6, max = 256)
    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;

    @NotBlank
    @Size(min = 0, max = 22)
    private String cpf;

    private Timestamp hireDate;

    private Roles roles;

    private boolean isAdmin;
    
    private BigDecimal salary;

    public BigDecimal getSalary() {
        return salary;
    }

    public void setSalary(BigDecimal salary) {
        this.salary = salary;
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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String nome) {
        this.userName = nome;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Employees users = (Employees) o;
        return Objects.equals(id, users.id) && Objects.equals(userName, users.userName) && Objects.equals(email, users.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userName, email);
    }

    public Long getTenantid() {
        return tenantId;
    }

    public void setTenantid(Long tenantid) {
        this.tenantId = tenantid;
    }

    @JsonProperty("isAdmin")
    public boolean isAdmin() {
        return isAdmin;
    }

    public void  setAdmin(boolean admin) {
    isAdmin = admin;}

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

    public Roles getRoles() {
        return roles;
    }

    public void setRoles(Roles roles) {
        this.roles = roles;
    }

    public UsersAccounts getUserAccounts() {
        return usersAccount;
    }

    public void setUserAccounts(UsersAccounts usersAccounts) {
        this.usersAccount = usersAccounts;
    }

    public String getPassword() {
        return password;
    }
}
