package com.erpapi.gzerp.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class EmployeeRegisterDto {

    @NotNull
    private Long tenantId;

    @NotBlank(message = "Please inform the Employee: Username")
    @Size(min = 1, max = 32)
    private String userName;

    @NotBlank(message = "Please inform the Employee: Name")
    private String fullName;

    @NotBlank(message = "Please inform the Employee: Password")
    @Size(min = 6, max = 20)
    private String password;

    @Email
    @NotBlank(message = "Please inform the Employee: Email")
    @Size(min = 6, max = 254)
    private String email;

    @NotBlank(message = "Please inform the Employee: Salary")
    private BigDecimal salary;

    @NotBlank(message = "Please inform the Employee: CPF")
    @Size(min = 11, max = 14)
    private String cpf;

    @NotBlank(message = "Please inform the Employee: Phone")
    @Size(min = 11, max = 15)
    private String phone;

    public EmployeeRegisterDto(UsersAccountRegisterDto dto) {
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
