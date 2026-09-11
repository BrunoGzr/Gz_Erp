//package com.erpapi.gzerp.config;
//
//import com.erpapi.gzerp.enums.Permissions;
//import com.erpapi.gzerp.enums.UserType;
//import com.erpapi.gzerp.models.UsersAccounts;
//import com.erpapi.gzerp.repositories.UsersAccountsRepo;
//import jakarta.validation.constraints.NotNull;
//import org.jspecify.annotations.NonNull;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.userdetails.User;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.HashSet;
//import java.util.Set;
//
//
//@Transactional
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
//
//    @Override
//    @Transactional(readOnly = true)
//    public UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
//        UsersAccounts user = usersAccountsRepo.findByUsername(username).orElseThrow(()-> new UsernameNotFoundException("User not found, please verify") );
//        return new User(user.getUsername(),user.getPassword(), setUserAuthorities(user) );
//    }
//
//    private Set<GrantedAuthority> setUserAuthorities(UsersAccounts user) {
//        Set<GrantedAuthority> authorities = new HashSet<>();
//
//        String baseRole = "ROLE_" + user.getUserType();
//
//        authorities.add(new SimpleGrantedAuthority(baseRole));
//
//        if(user.getUserType() == UserType.EMPLOYEE && user.getRole() != null){
//            for (Permissions permission : user.getRole().getPermissions()){
//                    authorities.add(new SimpleGrantedAuthority("PERM_" + permission.name()));
//            };
//        }
//
//        if (user.getUserType() == UserType.PARTNER || user.getUserType() == UserType.ADMIN){
//            for (Permissions permission : Permissions.values()){
//                authorities.add(new SimpleGrantedAuthority("PERM_" + permission));
//
//            }
//        }
//
//        return authorities;
//
//    }
//}
