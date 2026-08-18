package com.openclassrooms.datashare.service;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtServiceTest {

    private JwtService jwtService;
    private static final String EMAIL = "user@example.com";
    private static final String PASSWORD = "encoded-password";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();

        String secretKey = Base64.getEncoder()
                .encodeToString("01234567890123456789012345678901".getBytes(StandardCharsets.UTF_8));

        ReflectionTestUtils.setField(jwtService, "secretKey", secretKey);
        ReflectionTestUtils.setField(jwtService, "expirationMs", 3_600_000L);
    }

    private UserDetails userDetails() {
        return User.builder()
                .username(EMAIL)
                .password(PASSWORD)
                .authorities("USER")
                .build();
    }

    @Test
    void generateToken_returnsNonBlankToken() {
        assertThat(jwtService.generateToken(userDetails())).isNotBlank();
    }

    @Test
    void extractUsername_returnsEmailUsedAtGeneration() {
        String token = jwtService.generateToken(userDetails());
        assertThat(jwtService.extractUsername(token)).isEqualTo(EMAIL);
    }

    @Test
    void isTokenValid_withMatchingUser_returnsTrue() {
        String token = jwtService.generateToken(userDetails());
        assertThat(jwtService.isTokenValid(token, userDetails())).isTrue();
    }

    @Test
    void extractUsername_withExpiredToken_throwsExpiredJwtException() {
        ReflectionTestUtils.setField(jwtService, "expirationMs", -1000L);
        String token = jwtService.generateToken(userDetails());

        assertThrows(ExpiredJwtException.class, () -> jwtService.extractUsername(token));
    }

    @Test
    void extractUsername_withMalformedToken_throwsJwtException() {
        assertThrows(JwtException.class, () -> jwtService.extractUsername("not-a-valid-token"));
    }
}