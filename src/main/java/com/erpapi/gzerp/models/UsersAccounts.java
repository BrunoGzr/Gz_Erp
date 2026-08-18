package com.erpapi.gzerp.models;

import com.erpapi.gzerp.enums.AccountType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
public class UsersAccounts {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 100)
    @NotBlank
    @Email
    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @Size(max = 50)
    @NotBlank
    @Column(name = "username", nullable = false, length = 50)
    private String username;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(name = "user_type", nullable = false)
    private AccountType userType;

    @Size(max = 255)
    @NotBlank
    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @OneToOne(mappedBy = "userAccount", cascade = CascadeType.ALL, optional = true )
    private Partners partner;

    @OneToOne(mappedBy = "userAccount", cascade = CascadeType.ALL, optional = true )
    private Employees employee;


    public UsersAccounts() {
    }

    public Partners getPartner() {
        return partner;
    }

    public void setPartner(Partners partner) {
        this.partner = partner;
    }

    public Employees getEmployee() {
        return employee;
    }

    public void setEmployee(Employees employee) {
        this.employee = employee;
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

    public AccountType getUserType() {
        return userType;
    }

    public void setUserType(AccountType userType) {
        this.userType = userType;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }


}
