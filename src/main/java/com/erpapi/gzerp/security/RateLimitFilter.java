package com.erpapi.gzerp.security;

import com.erpapi.gzerp.config.LimitConfig;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.hibernate.query.spi.Limit;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static com.erpapi.gzerp.config.LimitConfig.ruleFor;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitingBucket bucket;


    public RateLimitFilter(RateLimitingBucket bucket) {
        this.bucket = bucket;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String ip = extractIp(request);
        String endpoint = request.getRequestURI();
        String key = "ip:" + ip + ":" + endpoint;

        LimitConfig.customRule rule = LimitConfig.ruleFor(endpoint);
        boolean allowed = bucket.allow(key,rule.limit(), rule.windowMs());


        if (!allowed){
            response.setStatus(429);
            response.setHeader("Retry-After", "60");
            response.setContentType("application/json");
            response.getWriter().write("""
                       {"title" : "Too many requests",
                        "status" : 429,
                        "detail" : "try again later."
                    }
                    """);
            return;
        }
        filterChain.doFilter(request,response);
    }


    private String extractIp(HttpServletRequest request){
        String forwarded = request.getHeader("X-Forwarded-For");
        if(forwarded != null && !forwarded.isBlank()){
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
