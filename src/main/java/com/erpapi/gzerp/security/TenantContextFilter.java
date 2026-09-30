package com.erpapi.gzerp.security;

import com.erpapi.gzerp.config.CustomUserDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(3)
public class TenantContextFilter extends OncePerRequestFilter {

    public TenantContextFilter() {
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails userDetails)){
            filterChain.doFilter(request,response);
            return;
        }

        try{
            ScopedValue.where(TenantContext.TENANT_ID, userDetails.getTenantId()).call(() -> {
                    filterChain.doFilter(request,response);
            return null;
            });
        }catch (ServletException | IOException e){
            throw e;
        }catch (Exception e){
            throw new ServletException(e);
        }};

    }
