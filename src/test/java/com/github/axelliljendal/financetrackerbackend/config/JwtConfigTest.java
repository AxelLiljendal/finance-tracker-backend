package com.github.axelliljendal.financetrackerbackend.config;

import com.github.axelliljendal.finance_tracker.PersonalFinanceTrackerApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = PersonalFinanceTrackerApplication.class)
@TestPropertySource(properties = {
        "jwt.secret=test-secret-key-for-testing",
        "jwt.expiration=86400000",
        "jwt.refresh-expiration=604800000",
        "jwt.header=Authorization",
        "jwt.prefix=Bearer "
})
class JwtConfigTest {

    @Value("${jwt.secret:}")
    private String jwtSecret;

    @Value("${jwt.expiration:0}")
    private Long expiration;

    @Value("${jwt.refresh-expiration:0}")
    private Long refreshExpiration;

    @Value("${jwt.header:}")
    private String header;

    @Value("${jwt.prefix:}")
    private String prefix;

    @Test
    void jwtConfigurationLoadsCorrectly() {
        assertNotNull(jwtSecret, "JWT secret should be loaded");
        assertFalse(jwtSecret.isEmpty(), "JWT secret should not be empty");
        assertEquals(86400000L, expiration, "Access token expiration should be 24 hours");
        assertEquals(604800000L, refreshExpiration, "Refresh token expiration should be 7 days");
        assertEquals("Authorization", header, "Header should be Authorization");
        assertEquals("Bearer ", prefix, "Prefix should be Bearer with space");
    }
}
