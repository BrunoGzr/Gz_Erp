package com.erpapi.gzerp.resources;

import com.erpapi.gzerp.dto.LoginRequestDto;
import com.erpapi.gzerp.dto.LoginResponseDto;
import com.erpapi.gzerp.services.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthResource {

    private final AuthService authService;

    public AuthResource(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto loginDto) {
        LoginResponseDto response = authService.authenticate(loginDto);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
