package com.erpapi.gzerp.resources;

import com.erpapi.gzerp.config.CustomUserDetails;
import com.erpapi.gzerp.config.JwtConfig;
import com.erpapi.gzerp.dto.*;
import com.erpapi.gzerp.exceptions.InvalidRefreshTokenException;
import com.erpapi.gzerp.models.RefreshToken;
import com.erpapi.gzerp.models.UsersAccounts;
import com.erpapi.gzerp.services.CustomUserDetailService;
import com.erpapi.gzerp.services.RefreshTokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/api/auth")
public class AuthResource {

    private final AuthenticationManager authenticationManager;
    private final JwtConfig jwtConfig;
    private final RefreshTokenService refreshTokenService;
    private final CustomUserDetailService customUserDetailService;

    @Value("${EM_PROD}")
    private Boolean mudarEmProd;

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

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        String accessToken = jwtConfig.generateToken(authentication);
        String refreshToken = refreshTokenService.create(userDetails.getUser()).getToken();

        ResponseCookie cookie = ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(mudarEmProd)
                .path("/")
                .maxAge(7*24*60*60)
                .sameSite("Strict")
                .build();



        LoginResponseDto responseDto = new LoginResponseDto(accessToken
                ,jwtConfig.getExpirationMs() / 1000
                ,userDetails.getUser().getPublicId()
                ,userDetails.getTenantId()
                ,userDetails.getUser().getUserType());


        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(responseDto);
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshTokenResponseDto> refresh(
            @CookieValue(name = "refresh_token", required = false) String refreshToken) {

        if (refreshToken == null || refreshToken.isBlank()){
            throw new InvalidRefreshTokenException("Refresh token cookie not founded.");
        }


        RefreshToken current = refreshTokenService.validate(refreshToken);
        RefreshToken refreshedToken = refreshTokenService.rotate(current);

        UsersAccounts user = current.getUserAccount();

        CustomUserDetails userDetails = customUserDetailService.loadUserByUsername(user.getUsername());

        String newAccessToken = jwtConfig.generateTokenFromUserDetails(userDetails);

        ResponseCookie cookie = ResponseCookie.from("refresh_token", refreshedToken.getToken())
                .httpOnly(true)
                .secure(mudarEmProd) // Mudar em produção para True, ou quando tiver o HTTPS
                .sameSite("Strict")
                .path("/")
                .maxAge(Duration.ofDays(7))
                .build();


        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE,cookie.toString())
                .body(new RefreshTokenResponseDto(newAccessToken,7 * 24 * 60 * 60));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@CookieValue(name = "refresh_token", required = false) String refreshToken){



        if (refreshToken != null && !refreshToken.isBlank()){
            try {
                RefreshToken token = refreshTokenService.validate(refreshToken);
                refreshTokenService.revoke(token);
            } catch (Exception _){
            }

        }


        ResponseCookie deletedCookie = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(mudarEmProd)//Mudar em prod
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();

        return ResponseEntity.noContent().header(HttpHeaders.SET_COOKIE, deletedCookie.toString())
                .build();
    }

}
