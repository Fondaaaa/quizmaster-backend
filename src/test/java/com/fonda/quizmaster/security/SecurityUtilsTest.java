package com.fonda.quizmaster.security;

import com.fonda.quizmaster.security.user.CustomUserDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SecurityUtilsTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUserId_whenAuthenticated_returnsUserId() {
        // Arrange
        var userDetails = new CustomUserDetails(
                55L,
                "testuser",
                "test@example.com",
                "hashedpassword",
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                true
        );
        var authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Act
        var userIdOpt = SecurityUtils.getCurrentUserId();

        // Assert
        assertThat(userIdOpt).contains(55L);
    }

    @Test
    void getCurrentUserId_whenUnauthenticated_returnsEmpty() {
        var userIdOpt = SecurityUtils.getCurrentUserId();

        assertThat(userIdOpt).isEmpty();
    }

    @Test
    void getRequiredCurrentUserId_whenAuthenticated_returnsUserId() {
        var userDetails = new CustomUserDetails(
                77L,
                "testuser",
                "test@example.com",
                "hashedpassword",
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                true
        );
        var authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        var userId = SecurityUtils.getRequiredCurrentUserId();

        assertThat(userId).isEqualTo(77L);
    }

    @Test
    void getRequiredCurrentUserId_whenUnauthenticated_throwsAuthenticationCredentialsNotFoundException() {
        assertThatThrownBy(SecurityUtils::getRequiredCurrentUserId)
                .isInstanceOf(AuthenticationCredentialsNotFoundException.class)
                .hasMessageContaining("No authenticated user found in SecurityContext");
    }

    @Test
    void getCurrentUserDetails_whenAuthenticated_returnsCustomUserDetails() {
        var userDetails = new CustomUserDetails(
                99L,
                "alice",
                "alice@example.com",
                "secret",
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                true
        );
        var authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        var detailsOpt = SecurityUtils.getCurrentUserDetails();

        assertThat(detailsOpt).contains(userDetails);
    }

    @Test
    void getCurrentUserDetails_whenUnauthenticated_returnsEmpty() {
        var detailsOpt = SecurityUtils.getCurrentUserDetails();

        assertThat(detailsOpt).isEmpty();
    }
}
