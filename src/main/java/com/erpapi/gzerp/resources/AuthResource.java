package com.erpapi.gzerp.resources;

import com.erpapi.gzerp.dto.LoginRequestDto;
import com.erpapi.gzerp.models.UsersAccounts;
import com.erpapi.gzerp.repositories.RolesRepo;
import com.erpapi.gzerp.repositories.UsersAccountsRepo;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class AuthResource {

    private AuthenticationManager authenticationManager;
    private UsersAccountsRepo usersAccountsRepo;
    private RolesRepo rolesRepo;
    private PasswordEncoder passwordEncoder;

    public AuthResource(AuthenticationManager authenticationManager, UsersAccountsRepo usersAccountsRepo, RolesRepo rolesRepo, PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.usersAccountsRepo = usersAccountsRepo;
        this.rolesRepo = rolesRepo;
        this.passwordEncoder = passwordEncoder;
    }


    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequestDto loginRequestDto){
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequestDto.getUsername(),loginRequestDto.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        return new ResponseEntity<>("User Signed In succesfull", HttpStatus.OK);
    }









}
