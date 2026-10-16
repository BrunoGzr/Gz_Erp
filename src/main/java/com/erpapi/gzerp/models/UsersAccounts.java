package com.erpapi.gzerp.models;

import com.erpapi.gzerp.dto.UsersAccountRegisterDto;
import com.erpapi.gzerp.enums.AccountStatus;
import com.erpapi.gzerp.enums.UserType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
public class UsersAccounts {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "public_id", nullable = false, unique = true, length = 36)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private UUID public_id;

    @Size(max = 256)
    @NotBlank
    @Email
    @Column(name = "email", nullable = false, length = 256, unique = true)
    private String email;

    @Size(max = 50)
    @NotBlank
    @Column(name = "username", nullable = false, length = 50, unique = true)
    private String username;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(name = "user_type", nullable = false)
    private UserType userType;

    @Size(max = 255)
    @NotBlank
    @Column(name = "password", nullable = false)
    private String password;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    @NotNull
    private Tenants tenant;

    @OneToOne(mappedBy = "usersAccounts",fetch = FetchType.LAZY  ,cascade = CascadeType.ALL, optional = true )
    private Partners partner;

    @OneToOne(mappedBy = "usersAccounts", cascade = CascadeType.ALL, optional = true )
    private Employees employee;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status")
    private AccountStatus status;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    private Roles role;



    public UsersAccounts() {
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }

    public Roles getRole() {
        return role;
    }

    public void setRole(Roles role) {
        this.role = role;
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

    public Tenants getTenant() {
        return tenant;
    }

    public void setTenant(Tenants tenant) {
        this.tenant = tenant;
    }

    public UserType getUserType() {
        return userType;
    }

    public void setUserType(UserType userType) {
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

    public UUID getPublic_id() {
        return public_id;
    }

    public void setPublic_id(UUID public_id) {
        this.public_id = public_id;
    }
}
