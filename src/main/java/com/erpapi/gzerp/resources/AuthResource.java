package com.erpapi.gzerp.resources;

import com.erpapi.gzerp.config.CustomUserDetails;
import com.erpapi.gzerp.config.JwtConfig;
import com.erpapi.gzerp.dto.*;
import com.erpapi.gzerp.models.RefreshToken;
import com.erpapi.gzerp.models.UsersAccounts;
import com.erpapi.gzerp.repositories.RolesRepo;
import com.erpapi.gzerp.repositories.UsersAccountsRepo;
import com.erpapi.gzerp.services.CustomUserDetailService;
import com.erpapi.gzerp.services.RefreshTokenService;
import jakarta.validation.Valid;
import org.springframework.boot.webmvc.autoconfigure.WebMvcProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthResource {

    private final AuthenticationManager authenticationManager;
    private final JwtConfig jwtConfig;
    private final RefreshTokenService refreshTokenService;
    private final CustomUserDetailService customUserDetailService;

    public AuthResource(AuthenticationManager authenticationManager, JwtConfig jwtConfig, RefreshTokenService refreshTokenService, CustomUserDetailService customUserDetailService) {
        this.authenticationManager = authenticationManager;
        this.jwtConfig = jwtConfig;
        this.refreshTokenService = refreshTokenService;
        this.customUserDetailService = customUserDetailService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto loginRequestDto){

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequestDto.getIdentifier(),loginRequestDto.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        UsersAccounts user = customUserDetails.getUser();
        String accessToken = jwtConfig.generateToken(authentication);

        RefreshToken refreshToken = refreshTokenService.create(user);

        LoginResponseDto response = new LoginResponseDto(accessToken,jwtConfig.getExpirationMs() / 1000 ,refreshToken.getToken(),user.getId(),user.getTenant().getId(),user.getUserType());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshTokenResponseDto> refresh(@RequestBody @Valid RefreshTokenRequestDto dto){
        RefreshToken current = refreshTokenService.validate(dto.getToken());
        RefreshToken refreshedToken = refreshTokenService.rotate(current);

        UsersAccounts user = current.getUserAccount();

        CustomUserDetails userDetails = (CustomUserDetails) customUserDetailService.loadUserByUsername(user.getUsername());

        String newAccessToken = jwtConfig.generateTokenFromUserDetails(userDetails);

        return ResponseEntity.ok(new RefreshTokenResponseDto(
                refreshedToken.getToken(),
                newAccessToken,
                "bearer",
                jwtConfig.getExpirationMs() / 1000
        ));



    }


    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody @Valid LogoutRequestDto dto){
        RefreshToken token = refreshTokenService.validate(dto.getRefreshToken());
        refreshTokenService.revoke(token);
        return ResponseEntity.noContent().build();
    }

}
