package com.erpapi.gzerp.config;

import org.apache.tomcat.util.digester.Rule;

import java.util.Map;

public class LimitConfig {

    private static final Map<String, customRule> Rules = Map.of(
            "/login", new customRule(5,60_000),
            "/refresh", new customRule(10,60_000),
            "/register", new customRule(3,3_600_000)
    );

    public static customRule ruleFor(String endpoint){
        return Rules.getOrDefault(endpoint, new customRule(100,60_000));
    }

    public record customRule(int limit,long windowMs){};
}


