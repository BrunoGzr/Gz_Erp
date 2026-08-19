package com.erpapi.gzerp.dto;

import com.erpapi.gzerp.models.Employees;

public class EmployeeResponseDto {

    private Long id;

    private String name;

    private Long tenantId;

    private String userName;

    private String email;


    public EmployeeResponseDto(Employees user) {
        this.id = user.getId();
        this.name = user.getFullName();
        this.tenantId = user.getTenantId();
        this.userName = user.getUserName();
        this.email = user.getEmail();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
