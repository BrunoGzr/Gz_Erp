package com.erpapi.gzerp.services;

import com.erpapi.gzerp.config.JwtConfig;
import com.erpapi.gzerp.exceptions.InvalidRefreshTokenException;
import com.erpapi.gzerp.models.RefreshToken;
import com.erpapi.gzerp.models.UsersAccounts;
import com.erpapi.gzerp.repositories.RefreshTokenRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepo refreshTokenRepo;
    private final JwtConfig jwtConfig;

    public RefreshTokenService(RefreshTokenRepo refreshTokenRepo, JwtConfig jwtConfig) {
        this.refreshTokenRepo = refreshTokenRepo;
        this.jwtConfig = jwtConfig;
    }

    @Transactional
    public RefreshToken create(UsersAccounts user){
        RefreshToken rt = new RefreshToken();

        rt.setToken(UUID.randomUUID().toString());
        rt.setUserAccount(user);
        rt.setCreatedAt(LocalDateTime.now());
        rt.setRevoked(false);
        rt.setExpiresAt(LocalDateTime.now().plusSeconds(jwtConfig.getExpirationMs() / 1000 ));
        return refreshTokenRepo.save(rt);
    }

    public RefreshToken validate(String token){
        RefreshToken rt = refreshTokenRepo.findByToken(token).orElseThrow(() -> new InvalidRefreshTokenException("Refresh Token not found."));

        if (rt.isRevoked()){
            throw new InvalidRefreshTokenException("Refresh token revoked.");
        }
        if (rt.getExpiresAt().isBefore(LocalDateTime.now())){
            throw new InvalidRefreshTokenException("Refresh token expired.");
        }
        return rt;

    }

    @Transactional
    public RefreshToken rotate(RefreshToken current){
        if (current.getReplacedBy() != null){
            refreshTokenRepo.revokeAllByUser(current.getUserAccount());
            throw new InvalidRefreshTokenException("Refresh token reuse detected. Fuck you bro");
        }
        current.setRevoked(true);

        RefreshToken newToken = create(current.getUserAccount());
        current.setReplacedBy(newToken.getToken());
        refreshTokenRepo.save(current);
        return newToken;

    }

    @Transactional
    public void revoke(RefreshToken token){
        token.setRevoked(true);
        refreshTokenRepo.save(token);
    }

    @Transactional
    public RefreshResult refresh(String token) {
        RefreshToken current = validate(token);
        RefreshToken rotated = rotate(current);
        UsersAccounts user = current.getUserAccount(); // dentro da transação
        return new RefreshResult(rotated, user);
    }

    public record RefreshResult(RefreshToken rotated, UsersAccounts user) {}

}
