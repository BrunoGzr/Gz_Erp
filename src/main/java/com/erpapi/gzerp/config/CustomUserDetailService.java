package com.erpapi.gzerp.config;

import com.erpapi.gzerp.models.UsersAccounts;
import com.erpapi.gzerp.repositories.UsersAccountsRepo;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password4j.BcryptPassword4jPasswordEncoder;
import org.springframework.stereotype.Service;


//@Service
//public class CustomUserDetailService implements UserDetailsService {
//
//    private final UsersAccountsRepo usersAccountsRepo;
//    private final PasswordEncoder passwordEncoder;
//
//
//    public CustomUserDetailService(PasswordEncoder passwordEncoder, UsersAccountsRepo usersAccountsRepo) {
//        this.passwordEncoder= passwordEncoder ;
//        this.usersAccountsRepo = usersAccountsRepo;
//    }
//
//    @Override
//    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//        UsersAccounts user = usersAccountsRepo.findByUsername(username).orElseThrow(()-> new UsernameNotFoundException("User not found, please verify") );
//
//
//        return null;
//    }
//}
