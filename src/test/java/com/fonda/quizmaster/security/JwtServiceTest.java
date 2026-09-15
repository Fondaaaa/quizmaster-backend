package com.fonda.quizmaster.security;

import com.fonda.quizmaster.security.jwt.JwtProperties;
import com.fonda.quizmaster.security.jwt.JwtService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import jakarta.servlet.http.Cookie;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String TEST_SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret(TEST_SECRET);
        properties.setExpiration(Duration.ofHours(1));
        properties.setCookieName("access_token");
        properties.setCookieSecure(false);
        properties.setCookieSameSite("Lax");

        jwtService = new JwtService(properties);
    }

    @Test
    void generateToken_withValidUserId_createsSignedJwtWithSubject() {
        // Arrange
        var userId = 42L;

        // Act
        var token = jwtService.generateToken(userId);

        // Assert
        assertThat(token).isNotBlank();
        assertThat(jwtService.validateToken(token)).isTrue();
        assertThat(jwtService.extractUserId(token)).isEqualTo(42L);
    }

    @Test
    void generateToken_whenUserIdIsNull_throwsNullPointerException() {
        assertThatThrownBy(() -> jwtService.generateToken(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("User ID cannot be null");
    }

    @Test
    void validateToken_whenTokenIsValid_returnsTrue() {
        var token = jwtService.generateToken(100L);

        var isValid = jwtService.validateToken(token);

        assertThat(isValid).isTrue();
    }

    @Test
    void validateToken_whenTokenIsExpired_returnsFalse() {
        // Arrange: generate token expired in the past
        var key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
        var past = Instant.now().minus(Duration.ofMinutes(10));
        var expiredToken = Jwts.builder()
                .subject("123")
                .issuedAt(Date.from(past.minus(Duration.ofMinutes(10))))
                .expiration(Date.from(past))
                .signWith(key)
                .compact();

        // Act
        var isValid = jwtService.validateToken(expiredToken);

        // Assert
        assertThat(isValid).isFalse();
    }

    @Test
    void validateToken_whenSignatureIsInvalid_returnsFalse() {
        // Arrange: token signed with a different key
        var differentSecret = "999E635266556A586E3272357538782F413F4428472B4B6250645367566B5999";
        var key = Keys.hmacShaKeyFor(differentSecret.getBytes(StandardCharsets.UTF_8));
        var foreignToken = Jwts.builder()
                .subject("123")
                .issuedAt(new Date())
                .expiration(Date.from(Instant.now().plus(Duration.ofHours(1))))
                .signWith(key)
                .compact();

        // Act
        var isValid = jwtService.validateToken(foreignToken);

        // Assert
        assertThat(isValid).isFalse();
    }

    @Test
    void validateToken_whenTokenIsMalformed_returnsFalse() {
        var isValid = jwtService.validateToken("not-a-valid-jwt-token");

        assertThat(isValid).isFalse();
    }

    @Test
    void validateToken_whenTokenIsNullOrBlank_returnsFalse() {
        assertThat(jwtService.validateToken(null)).isFalse();
        assertThat(jwtService.validateToken("")).isFalse();
        assertThat(jwtService.validateToken("   ")).isFalse();
    }

    @Test
    void extractTokenFromCookie_whenCookiePresent_returnsToken() {
        var request = new MockHttpServletRequest();
        request.setCookies(new Cookie("access_token", "sample-token-abc"));

        var tokenOpt = jwtService.extractTokenFromCookie(request);

        assertThat(tokenOpt).contains("sample-token-abc");
    }

    @Test
    void extractTokenFromCookie_whenCookieMissing_returnsEmpty() {
        var request = new MockHttpServletRequest();
        request.setCookies(new Cookie("other_cookie", "sample-value"));

        var tokenOpt = jwtService.extractTokenFromCookie(request);

        assertThat(tokenOpt).isEmpty();
    }

    @Test
    void extractTokenFromCookie_whenCookiesNull_returnsEmpty() {
        var request = new MockHttpServletRequest();

        var tokenOpt = jwtService.extractTokenFromCookie(request);

        assertThat(tokenOpt).isEmpty();
    }

    @Test
    void createTokenCookie_withValidToken_returnsHttpOnlyCookie() {
        var cookie = jwtService.createTokenCookie("my-jwt-token");

        assertThat(cookie.getName()).isEqualTo("access_token");
        assertThat(cookie.getValue()).isEqualTo("my-jwt-token");
        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(cookie.isSecure()).isFalse();
        assertThat(cookie.getSameSite()).isEqualTo("Lax");
        assertThat(cookie.getPath()).isEqualTo("/");
        assertThat(cookie.getMaxAge()).isEqualTo(Duration.ofHours(1));
    }

    @Test
    void clearTokenCookie_returnsCookieWithZeroMaxAge() {
        var cookie = jwtService.clearTokenCookie();

        assertThat(cookie.getName()).isEqualTo("access_token");
        assertThat(cookie.getValue()).isEmpty();
        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(cookie.getMaxAge()).isEqualTo(Duration.ZERO);
    }
}
