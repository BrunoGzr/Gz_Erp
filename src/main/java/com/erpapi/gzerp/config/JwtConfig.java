package com.erpapi.gzerp.config;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;

@Service
public class JwtConfig {

    @Value("${JWT_SECRET}")
    private String secretKey;

    @Value("${JWT_EXPIRATION_MS}")
    private long jwtExpiration;

    @Value("${JWT_REFRESH_EXPIRATION_MS}")
    private long refreshExpirationMs;

    public String generateToken(Authentication authentication){
        return generateTokenFromUserDetails((CustomUserDetails) authentication.getPrincipal());
    }

    public String generateTokenFromUserDetails(CustomUserDetails user){
        List<String> roles = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder()
                .subject(user.getUsername())
                .claim("id", user.getId())
                .claim("tenantId", user.getTenantId())
                .claim("userType", user.getUserType().name())
                .claim("roles", roles )
                .claim("email", user.getEmail())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey())
                .compact();
    }


    public Claims extractAllClaims(String token){
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long extractId(String token){
        return extractAllClaims(token).get("id",Long.class);
    }

    public String extractUsername(String token){
        return extractAllClaims(token).getSubject();
    }

    public Long extractTenantId(String token){
        return extractAllClaims(token).get("tenantId", Long.class);
    }

    public String extractUserType(String token){
        return extractAllClaims(token).get("userType",String.class);
    }

    public Date extractExpiration(String token){
        return extractAllClaims(token).getExpiration();
    }

    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token){
        return extractAllClaims(token).get("roles", List.class);
    }

    public boolean isTokenExpired(String token){
        return extractExpiration(token).before(new Date());
    }

    public boolean isTokenValid (String token, CustomUserDetails userDetails){
        final String username = extractUsername(token);

        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);

    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public long getExpirationMs() {
        return jwtExpiration;
    }
}
