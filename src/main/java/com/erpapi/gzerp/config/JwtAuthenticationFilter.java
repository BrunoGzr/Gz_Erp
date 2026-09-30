package com.erpapi.gzerp.config;

import com.erpapi.gzerp.services.CustomUserDetailService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(2)
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtConfig jwtConfig;

    private final CustomUserDetailService customUserDetailService;


    public JwtAuthenticationFilter(JwtConfig jwtConfig, CustomUserDetailService customUserDetailService) {
        this.jwtConfig = jwtConfig;
        this.customUserDetailService = customUserDetailService;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {


        String token = getJWTFromRequest(request);
        if (!StringUtils.hasText(token)) {
            filterChain.doFilter(request, response);
            return;
        }
        try {
            String username = jwtConfig.extractUsername(token);
            if (!StringUtils.hasText(username)) {
                sendUnauthorized(response,request, "Username is Empty.");
                return;
            }

            CustomUserDetails userDetails = customUserDetailService.loadUserByUsername(jwtConfig.extractUsername(token));

            if (jwtConfig.isTokenValid(token, userDetails)) {
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            } else {
                sendUnauthorized(response,request, "Invalid or Expired token.");
                return;
            }
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            sendUnauthorized(response, request,"Token Expired");
            return;
        } catch (io.jsonwebtoken.security.SignatureException
                | io.jsonwebtoken.MalformedJwtException
                | org.springframework.security.core.userdetails.UsernameNotFoundException ex ) {
            sendUnauthorized(response, request,"Invalid or Expired token");
            return;
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
            throw e;
        }
        filterChain.doFilter(request, response);
    }

    private void sendUnauthorized(HttpServletResponse response, HttpServletRequest request, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/problem+json");
        response.setCharacterEncoding("UTF-8");

        String body = """
                {
                "type": "about:blank",
                "title": "Bad Credentials",
                "status": 401,
                "detail":"%s",
                "instance": "%s"
                } """.formatted(message,request.getRequestURI());

        response.getWriter().write(body);
    }

    private String getJWTFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

}
