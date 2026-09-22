package com.erpapi.gzerp.config;


import com.erpapi.gzerp.enums.UserType;
import com.erpapi.gzerp.models.UsersAccounts;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import org.springframework.security.core.GrantedAuthority;

public class CustomUserDetails implements UserDetails {

    private final UsersAccounts user;
    private final Set<GrantedAuthority> authorities;

    public CustomUserDetails(UsersAccounts user, Set<GrantedAuthority> authorities) {
        this.user = user;
        this.authorities = authorities;
    }

    public long getId() {
        return user.getId();
    }

    public long getTenantId(){
        return user.getTenant().getId();
    }

    public UserType getUserType(){
        return user.getUserType();
    }

    public String getEmail(){
        return user.getEmail();
    }

    public UsersAccounts getUser(){
        return user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities(){
        return authorities;
    }

    @Override
    public String getUsername(){
        return user.getUsername();
    }

    @Override
    public String getPassword(){
        return user.getPassword();
    }

    @Override
    public boolean isAccountNonExpired(){
        return true;
    }

    @Override
    public boolean isAccountNonLocked(){
        return true;
    }

    @Override
    public boolean isEnabled(){
        return true;
    }



}
