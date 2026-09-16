package com.fonda.quizmaster.auth;

import com.fonda.quizmaster.auth.dto.LoginRequest;
import com.fonda.quizmaster.auth.dto.RegisterRequest;
import com.fonda.quizmaster.common.exception.DuplicateResourceException;
import com.fonda.quizmaster.common.exception.ResourceNotFoundException;
import com.fonda.quizmaster.security.jwt.JwtService;
import com.fonda.quizmaster.security.user.CustomUserDetails;
import com.fonda.quizmaster.user.Role;
import com.fonda.quizmaster.user.User;
import com.fonda.quizmaster.user.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private ResponseCookie sampleCookie;

    @BeforeEach
    void setUp() {
        sampleCookie = ResponseCookie.from("access_token", "sample.jwt.token")
                .httpOnly(true)
                .path("/")
                .maxAge(Duration.ofHours(1))
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void register_whenValidRequest_createsUserAndReturnsAuthResultWithCookie() {
        // Arrange
        var request = new RegisterRequest("alice", "alice@example.com", "Password123!");
        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("encodedPassword123");

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            ReflectionTestUtils.setField(user, "id", 1L);
            return user;
        });

        when(jwtService.generateToken(1L)).thenReturn("sample.jwt.token");
        when(jwtService.createTokenCookie("sample.jwt.token")).thenReturn(sampleCookie);

        // Act
        var result = authService.register(request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.user()).isNotNull();
        assertThat(result.user().id()).isEqualTo(1L);
        assertThat(result.user().username()).isEqualTo("alice");
        assertThat(result.user().email()).isEqualTo("alice@example.com");
        assertThat(result.user().role()).isEqualTo(Role.USER);
        assertThat(result.cookie()).isEqualTo(sampleCookie);

        var userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        var savedUser = userCaptor.getValue();
        assertThat(savedUser.getUsername()).isEqualTo("alice");
        assertThat(savedUser.getEmail()).isEqualTo("alice@example.com");
        assertThat(savedUser.getPassword()).isEqualTo("encodedPassword123");
        assertThat(savedUser.getRole()).isEqualTo(Role.USER);
    }

    @Test
    void register_whenUsernameAlreadyExists_throwsDuplicateResourceException() {
        // Arrange
        var request = new RegisterRequest("alice", "alice@example.com", "Password123!");
        when(userRepository.existsByUsername("alice")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Username");

        verify(userRepository, never()).save(any());
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void register_whenEmailAlreadyExists_throwsDuplicateResourceException() {
        // Arrange
        var request = new RegisterRequest("alice", "alice@example.com", "Password123!");
        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Email");

        verify(userRepository, never()).save(any());
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void login_whenValidCredentials_authenticatesAndReturnsAuthResultWithCookie() {
        // Arrange
        var request = new LoginRequest("alice", "Password123!");
        var userDetails = new CustomUserDetails(
                1L,
                "alice",
                "alice@example.com",
                "encodedPassword",
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                true
        );
        var authResultMock = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authResultMock);

        var userEntity = new User("alice", "alice@example.com", "encodedPassword", Role.USER);
        ReflectionTestUtils.setField(userEntity, "id", 1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));

        when(jwtService.generateToken(1L)).thenReturn("sample.jwt.token");
        when(jwtService.createTokenCookie("sample.jwt.token")).thenReturn(sampleCookie);

        // Act
        var result = authService.login(request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.user().id()).isEqualTo(1L);
        assertThat(result.user().username()).isEqualTo("alice");
        assertThat(result.cookie()).isEqualTo(sampleCookie);
    }

    @Test
    void login_whenBadCredentials_throwsAuthenticationException() {
        // Arrange
        var request = new LoginRequest("alice", "WrongPassword");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // Act & Assert
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid credentials");

        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void getCurrentUser_whenAuthenticated_returnsUserDto() {
        // Arrange
        var userDetails = new CustomUserDetails(
                1L,
                "alice",
                "alice@example.com",
                "encodedPassword",
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                true
        );
        var auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        var userEntity = new User("alice", "alice@example.com", "encodedPassword", Role.USER);
        ReflectionTestUtils.setField(userEntity, "id", 1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));

        // Act
        var result = authService.getCurrentUser();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.username()).isEqualTo("alice");
        assertThat(result.email()).isEqualTo("alice@example.com");
    }

    @Test
    void getCurrentUser_whenUserNotFoundInRepository_throwsResourceNotFoundException() {
        // Arrange
        var userDetails = new CustomUserDetails(
                999L,
                "ghost",
                "ghost@example.com",
                "encodedPassword",
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                true
        );
        var auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authService.getCurrentUser())
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void logout_returnsClearTokenCookie() {
        // Arrange
        var clearCookie = ResponseCookie.from("access_token", "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .build();
        when(jwtService.clearTokenCookie()).thenReturn(clearCookie);

        // Act
        var result = authService.logout();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getMaxAge().getSeconds()).isZero();
        assertThat(result.getValue()).isEmpty();
    }
}
