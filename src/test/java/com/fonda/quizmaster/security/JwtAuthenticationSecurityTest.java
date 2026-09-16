package com.fonda.quizmaster.security;

import com.fonda.quizmaster.security.jwt.JwtService;
import com.fonda.quizmaster.security.user.CustomUserDetails;
import com.fonda.quizmaster.security.user.CustomUserDetailsService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(JwtAuthenticationSecurityTest.TestSecurityController.class)
class JwtAuthenticationSecurityTest {

    private static final String TEST_SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private CustomUserDetails activeUserDetails;

    @BeforeEach
    void setUp() {
        activeUserDetails = new CustomUserDetails(
                10L,
                "alice",
                "alice@example.com",
                "encodedPassword",
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                true
        );
    }

    @Test
    void protectedEndpoint_whenValidCookieProvided_returns200AndAuthenticatedUserId() throws Exception {
        // Arrange
        when(customUserDetailsService.loadUserById(10L)).thenReturn(activeUserDetails);
        var token = jwtService.generateToken(10L);

        // Act & Assert
        mockMvc.perform(get("/api/test/protected")
                        .cookie(new Cookie("access_token", token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(10))
                .andExpect(jsonPath("$.username").value("alice"));
    }

    @Test
    void meEndpoint_whenValidCookieProvided_resolvesDatabaseUserIdViaSecurityUtils() throws Exception {
        when(customUserDetailsService.loadUserById(10L)).thenReturn(activeUserDetails);
        var token = jwtService.generateToken(10L);

        mockMvc.perform(get("/api/test/me")
                        .cookie(new Cookie("access_token", token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentUserId").value(10));
    }

    @Test
    void protectedEndpoint_whenCookieIsMissing_returns401ProblemDetail() throws Exception {
        mockMvc.perform(get("/api/test/protected"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.type").value("about:blank"))
                .andExpect(jsonPath("$.title").value("Unauthorized"))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.detail").value("Full authentication is required to access this resource"));
    }

    @Test
    void authMeEndpoint_whenUnauthenticated_returns401ProblemDetail() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.type").value("about:blank"))
                .andExpect(jsonPath("$.title").value("Unauthorized"))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.detail").value("Full authentication is required to access this resource"));
    }

    @Test
    void protectedEndpoint_whenCookieIsExpired_returns401ProblemDetail() throws Exception {
        // Arrange: expired token
        var key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
        var past = Instant.now().minus(Duration.ofMinutes(15));
        var expiredToken = Jwts.builder()
                .subject("10")
                .issuedAt(Date.from(past.minus(Duration.ofMinutes(10))))
                .expiration(Date.from(past))
                .signWith(key)
                .compact();

        // Act & Assert
        mockMvc.perform(get("/api/test/protected")
                        .cookie(new Cookie("access_token", expiredToken)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("Unauthorized"))
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void protectedEndpoint_whenCookieSignatureIsInvalid_returns401ProblemDetail() throws Exception {
        var wrongKeySecret = "999E635266556A586E3272357538782F413F4428472B4B6250645367566B5999";
        var foreignKey = Keys.hmacShaKeyFor(wrongKeySecret.getBytes(StandardCharsets.UTF_8));
        var tamperedToken = Jwts.builder()
                .subject("10")
                .issuedAt(new Date())
                .expiration(Date.from(Instant.now().plus(Duration.ofHours(1))))
                .signWith(foreignKey)
                .compact();

        mockMvc.perform(get("/api/test/protected")
                        .cookie(new Cookie("access_token", tamperedToken)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("Unauthorized"))
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void protectedEndpoint_whenUserNotFoundInRepository_returns401ProblemDetail() throws Exception {
        when(customUserDetailsService.loadUserById(999L))
                .thenThrow(new UsernameNotFoundException("User not found with id: 999"));
        var token = jwtService.generateToken(999L);

        mockMvc.perform(get("/api/test/protected")
                        .cookie(new Cookie("access_token", token)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("Unauthorized"))
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void protectedEndpoint_whenUserIsInactive_returns401ProblemDetail() throws Exception {
        when(customUserDetailsService.loadUserById(20L))
                .thenThrow(new DisabledException("User account is disabled"));
        var token = jwtService.generateToken(20L);

        mockMvc.perform(get("/api/test/protected")
                        .cookie(new Cookie("access_token", token)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("Unauthorized"))
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void currentUserAction_whenClientSuppliesDifferentUserId_strictlyUsesAuthenticatedDatabaseUserId() throws Exception {
        // Arrange
        when(customUserDetailsService.loadUserById(10L)).thenReturn(activeUserDetails);
        var token = jwtService.generateToken(10L);

        // Client attempts to supply another user's ID (e.g. 999) as a request param
        mockMvc.perform(post("/api/test/current-user-action")
                        .param("clientSuppliedId", "999")
                        .cookie(new Cookie("access_token", token))
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.effectiveUserId").value(10))
                .andExpect(jsonPath("$.clientSuppliedIdIgnored").value(true));
    }

    @RestController
    @RequestMapping("/api/test")
    static class TestSecurityController {

        @GetMapping("/protected")
        public Map<String, Object> getProtected(@AuthenticationPrincipal CustomUserDetails userDetails) {
            return Map.of(
                    "userId", userDetails.getId(),
                    "username", userDetails.getUsername()
            );
        }

        @GetMapping("/me")
        public Map<String, Object> getMe() {
            return Map.of("currentUserId", SecurityUtils.getRequiredCurrentUserId());
        }

        @PostMapping("/current-user-action")
        public Map<String, Object> currentUserAction(
                @RequestParam(required = false) Long clientSuppliedId,
                @AuthenticationPrincipal CustomUserDetails userDetails
        ) {
            return Map.of(
                    "effectiveUserId", SecurityUtils.getRequiredCurrentUserId(),
                    "clientSuppliedIdIgnored", clientSuppliedId != null && !clientSuppliedId.equals(userDetails.getId())
            );
        }
    }
}
