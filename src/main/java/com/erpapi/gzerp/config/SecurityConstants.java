package com.erpapi.gzerp.config;

import org.springframework.beans.factory.annotation.Value;

public class SecurityConstants {
    public static final long JwtExpirantion = 70000;
    @Value("${JWT_SECRET}")
    public static String JwtSecret;
}
