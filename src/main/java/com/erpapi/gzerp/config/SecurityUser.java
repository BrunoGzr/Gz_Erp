package com.erpapi.gzerp.config;

import com.erpapi.gzerp.models.Employees;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class SecurityUser implements UserDetails {

    private final Employees employees;

    public SecurityUser(Employees employees) {
        this.employees = employees;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (employees.isAdmin()) {
            return List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return employees.getPassword();
    }

    @Override
    public String getUsername() {
        return employees.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public Long getTenantId() {
        return employees.getTenantId();
    }

    public Employees getEmployees() {
        return employees;
    }
}
