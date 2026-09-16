package com.fonda.quizmaster.auth;

import com.fonda.quizmaster.auth.dto.LoginRequest;
import com.fonda.quizmaster.auth.dto.RegisterRequest;
import com.fonda.quizmaster.user.dto.UserDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Authentication", description = "Endpoints for user registration, authentication, logout, and identity")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Register a new user", description = "Creates a new user account and automatically sets an HTTP-only JWT access cookie.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User successfully registered and authenticated"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload or validation failure"),
            @ApiResponse(responseCode = "409", description = "Username or email already exists")
    })
    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@Valid @RequestBody RegisterRequest request) {
        var result = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .header(HttpHeaders.SET_COOKIE, result.cookie().toString())
                .body(result.user());
    }

    @Operation(summary = "Log in user", description = "Authenticates user credentials and issues an HTTP-only JWT access cookie.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully authenticated"),
            @ApiResponse(responseCode = "400", description = "Missing or malformed credentials"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials or disabled account")
    })
    @PostMapping("/login")
    public ResponseEntity<UserDto> login(@Valid @RequestBody LoginRequest request) {
        var result = authService.login(request);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, result.cookie().toString())
                .body(result.user());
    }

    @Operation(summary = "Log out user", description = "Clears the HTTP-only JWT access cookie.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Successfully logged out")
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        var cookie = authService.logout();
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }

    @Operation(summary = "Get current user profile", description = "Returns the profile of the currently authenticated user based on JWT cookie.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Current authenticated user profile"),
            @ApiResponse(responseCode = "401", description = "Unauthenticated or invalid token"),
            @ApiResponse(responseCode = "404", description = "Authenticated user not found in database")
    })
    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser() {
        return ResponseEntity.ok(authService.getCurrentUser());
    }
}
