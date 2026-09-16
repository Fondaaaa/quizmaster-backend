package com.fonda.quizmaster.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fonda.quizmaster.auth.dto.AuthResult;
import com.fonda.quizmaster.auth.dto.LoginRequest;
import com.fonda.quizmaster.auth.dto.RegisterRequest;
import com.fonda.quizmaster.common.exception.DuplicateResourceException;
import com.fonda.quizmaster.common.exception.GlobalExceptionHandler;
import com.fonda.quizmaster.common.exception.ResourceNotFoundException;
import com.fonda.quizmaster.user.Role;
import com.fonda.quizmaster.user.dto.UserDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private com.fonda.quizmaster.security.jwt.JwtService jwtService;

    @MockitoBean
    private com.fonda.quizmaster.security.user.CustomUserDetailsService customUserDetailsService;

    @Test
    void register_whenValidRequest_returns201AndSetCookieAndUserDto() throws Exception {
        // Arrange
        var request = new RegisterRequest("alice", "alice@example.com", "Password123!");
        var userDto = new UserDto(1L, "alice", "alice@example.com", Role.USER, Instant.now());
        var cookie = ResponseCookie.from("access_token", "sample.jwt.token")
                .httpOnly(true)
                .path("/")
                .maxAge(Duration.ofHours(1))
                .build();
        when(authService.register(any(RegisterRequest.class))).thenReturn(new AuthResult(userDto, cookie));

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.containsString("access_token=sample.jwt.token")))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.email").value("alice@example.com"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void register_whenUsernameIsBlank_returns400ProblemDetailWithFieldErrors() throws Exception {
        // Arrange
        var request = new RegisterRequest("", "alice@example.com", "Password123!");

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("about:blank"))
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.username").exists());
    }

    @Test
    void register_whenEmailIsInvalid_returns400ProblemDetailWithFieldErrors() throws Exception {
        // Arrange
        var request = new RegisterRequest("alice", "not-an-email", "Password123!");

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.email").exists());
    }

    @Test
    void register_whenPasswordTooShort_returns400ProblemDetailWithFieldErrors() throws Exception {
        // Arrange
        var request = new RegisterRequest("alice", "alice@example.com", "short");

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.password").exists());
    }

    @Test
    void register_whenDuplicateUser_returns409ProblemDetail() throws Exception {
        // Arrange
        var request = new RegisterRequest("alice", "alice@example.com", "Password123!");
        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new DuplicateResourceException("Username 'alice' already exists"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type").value("about:blank"))
                .andExpect(jsonPath("$.title").value("Conflict"))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail").value("Username 'alice' already exists"));
    }

    @Test
    void login_whenValidCredentials_returns200AndSetCookieAndUserDto() throws Exception {
        // Arrange
        var request = new LoginRequest("alice", "Password123!");
        var userDto = new UserDto(1L, "alice", "alice@example.com", Role.USER, Instant.now());
        var cookie = ResponseCookie.from("access_token", "sample.jwt.token")
                .httpOnly(true)
                .path("/")
                .maxAge(Duration.ofHours(1))
                .build();
        when(authService.login(any(LoginRequest.class))).thenReturn(new AuthResult(userDto, cookie));

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.containsString("access_token=sample.jwt.token")))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("alice"));
    }

    @Test
    void login_whenMissingFields_returns400ProblemDetailWithFieldErrors() throws Exception {
        // Arrange
        var request = new LoginRequest("", "");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.username").exists())
                .andExpect(jsonPath("$.errors.password").exists());
    }

    @Test
    void logout_returns204AndClearedCookie() throws Exception {
        // Arrange
        var clearCookie = ResponseCookie.from("access_token", "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .build();
        when(authService.logout()).thenReturn(clearCookie);

        // Act & Assert
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isNoContent())
                .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.containsString("Max-Age=0")));
    }

    @Test
    void getCurrentUser_whenFound_returns200AndUserDto() throws Exception {
        // Arrange
        var userDto = new UserDto(1L, "alice", "alice@example.com", Role.USER, Instant.now());
        when(authService.getCurrentUser()).thenReturn(userDto);

        // Act & Assert
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.email").value("alice@example.com"));
    }

    @Test
    void getCurrentUser_whenNotFound_returns404ProblemDetail() throws Exception {
        // Arrange
        when(authService.getCurrentUser())
                .thenThrow(new ResourceNotFoundException("User not found with id: 1"));

        // Act & Assert
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("about:blank"))
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("User not found with id: 1"));
    }
}
