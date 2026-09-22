package com.erpapi.gzerp.resources;

import com.erpapi.gzerp.config.JwtConfig;
import com.erpapi.gzerp.dto.LoginRequestDto;
import com.erpapi.gzerp.dto.LoginResponseDto;
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
public class AuthResource {

    private AuthenticationManager authenticationManager;
    private UsersAccountsRepo usersAccountsRepo;
    private RolesRepo rolesRepo;
    private PasswordEncoder passwordEncoder;
    private JwtConfig jwtConfig;

    public AuthResource(AuthenticationManager authenticationManager, UsersAccountsRepo usersAccountsRepo, RolesRepo rolesRepo, PasswordEncoder passwordEncoder, JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
        this.authenticationManager = authenticationManager;
        this.usersAccountsRepo = usersAccountsRepo;
        this.rolesRepo = rolesRepo;
        this.passwordEncoder = passwordEncoder;
    }


    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto loginRequestDto){
        System.out.println(">>> username recebido: [" + loginRequestDto.getUsername() + "]");
        System.out.println(">>> tamanho: " + loginRequestDto.getUsername().length());


        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequestDto.getUsername(),loginRequestDto.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = jwtConfig.generateToken(authentication);

        LoginResponseDto response = new LoginResponseDto(token,jwtConfig.getExpirationMs());

        return ResponseEntity.ok(response);
    }

}
