package com.erpapi.gzerp.services;

import com.erpapi.gzerp.config.CustomUserDetails;
import com.erpapi.gzerp.enums.Permissions;
import com.erpapi.gzerp.enums.UserType;
import com.erpapi.gzerp.models.UsersAccounts;
import com.erpapi.gzerp.repositories.UsersAccountsRepo;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;


@Transactional
@Service
public class CustomUserDetailService implements UserDetailsService {

    private final UsersAccountsRepo usersAccountsRepo;
    private final PasswordEncoder passwordEncoder;


    public CustomUserDetailService(PasswordEncoder passwordEncoder, UsersAccountsRepo usersAccountsRepo) {
        this.passwordEncoder= passwordEncoder ;
        this.usersAccountsRepo = usersAccountsRepo;
    }


    @Override
    @Transactional(readOnly = true)
    public CustomUserDetails loadUserByUsername(@NonNull String identifier) throws UsernameNotFoundException {
        UsersAccounts user = usersAccountsRepo.findByUsernameOrEmail(identifier,identifier).orElseThrow(()-> new UsernameNotFoundException("User not found"));

        return new CustomUserDetails(user, buildAuthorities(user));
    }

    private Set<GrantedAuthority> buildAuthorities(UsersAccounts user) {
        Set<GrantedAuthority> authorities = new HashSet<>();

        String baseRole = "ROLE_" + user.getUserType();

        authorities.add(new SimpleGrantedAuthority(baseRole));

        if(user.getUserType() == UserType.EMPLOYEE && user.getRole() != null){
            for (Permissions permission : user.getRole().getPermissions()){
                    authorities.add(new SimpleGrantedAuthority("PERM_" + permission.name()));
            }
        }

        if (user.getUserType() == UserType.PARTNER || user.getUserType() == UserType.ADMIN){
            for (Permissions permission : Permissions.values()){
                authorities.add(new SimpleGrantedAuthority("PERM_" + permission.name()));

            }
        }

        return authorities;

    }
}
