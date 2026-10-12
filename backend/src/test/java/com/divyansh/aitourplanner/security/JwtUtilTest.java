package com.divyansh.aitourplanner.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    private JwtUtil jwtUtil;
    private String testSecret = "this-is-a-very-long-secret-key-for-testing-purposes-only-32-chars-min";
    private long expirationMs = 3600000; // 1 hour

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", testSecret);
        ReflectionTestUtils.setField(jwtUtil, "expirationMs", expirationMs);
        jwtUtil.init();
    }

    @Test
    void generateToken_createsValidTokenWithEmail() {
        String token = jwtUtil.generateToken("user@example.com");

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts
    }

    @Test
    void extractEmail_returnsEmailFromToken() {
        String token = jwtUtil.generateToken("user@example.com");

        String email = jwtUtil.extractEmail(token);

        assertThat(email).isEqualTo("user@example.com");
    }

    @Test
    void isTokenValid_validToken_returnsTrue() {
        String token = jwtUtil.generateToken("user@example.com");

        assertThat(jwtUtil.isTokenValid(token)).isTrue();
    }

    @Test
    void isTokenValid_expiredToken_returnsFalse() {
        // Create a token with past expiration
        JwtUtil expiredJwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(expiredJwtUtil, "secret", testSecret);
        ReflectionTestUtils.setField(expiredJwtUtil, "expirationMs", -10000); // Expired 10 seconds ago
        expiredJwtUtil.init();

        String expiredToken = expiredJwtUtil.generateToken("user@example.com");

        assertThat(expiredJwtUtil.isTokenValid(expiredToken)).isFalse();
    }

    @Test
    void isTokenValid_invalidSignature_returnsFalse() {
        // Create token with different secret
        String otherSecret = "another-very-long-secret-key-for-testing-purposes-only-32-chars-min";
        SecretKey otherKey = Keys.hmacShaKeyFor(otherSecret.getBytes(StandardCharsets.UTF_8));

        String token = Jwts.builder()
                .subject("user@example.com")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(otherKey)
                .compact();

        assertThat(jwtUtil.isTokenValid(token)).isFalse();
    }

    @Test
    void isTokenValid_malformedToken_returnsFalse() {
        assertThat(jwtUtil.isTokenValid("not.a.valid.token")).isFalse();
        assertThat(jwtUtil.isTokenValid("")).isFalse();
        assertThat(jwtUtil.isTokenValid(null)).isFalse();
    }

    @Test
    void init_nullSecret_throwsException() {
        JwtUtil util = new JwtUtil();
        ReflectionTestUtils.setField(util, "secret", null);
        ReflectionTestUtils.setField(util, "expirationMs", expirationMs);

        assertThatThrownBy(util::init)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("JWT secret is not configured");
    }

    @Test
    void init_shortSecret_throwsException() {
        JwtUtil util = new JwtUtil();
        ReflectionTestUtils.setField(util, "secret", "short");
        ReflectionTestUtils.setField(util, "expirationMs", expirationMs);

        assertThatThrownBy(util::init)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("at least 32 characters");
    }
}