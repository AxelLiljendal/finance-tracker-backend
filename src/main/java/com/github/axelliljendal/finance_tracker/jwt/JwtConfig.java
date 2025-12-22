package com.github.axelliljendal.finance_tracker.jwt;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class JwtConfig {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration:86400000}") // 24 hours default
    private long expiration;

    @Value("${jwt.refresh-expiration:604800000}") // 7 days default
    private long refreshExpiration; // You're missing this variable declaration!

    @Value("${jwt.header:Authorization}")
    private String header;

    @Value("${jwt.prefix:Bearer }")
    private String prefix;
}