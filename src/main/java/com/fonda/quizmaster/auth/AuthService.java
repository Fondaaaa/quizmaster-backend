package com.fonda.quizmaster.auth;

import com.fonda.quizmaster.auth.dto.AuthResult;
import com.fonda.quizmaster.auth.dto.LoginRequest;
import com.fonda.quizmaster.auth.dto.RegisterRequest;
import com.fonda.quizmaster.common.exception.DuplicateResourceException;
import com.fonda.quizmaster.common.exception.ResourceNotFoundException;
import com.fonda.quizmaster.security.SecurityUtils;
import com.fonda.quizmaster.security.jwt.JwtService;
import com.fonda.quizmaster.security.user.CustomUserDetails;
import com.fonda.quizmaster.user.Role;
import com.fonda.quizmaster.user.User;
import com.fonda.quizmaster.user.UserRepository;
import com.fonda.quizmaster.user.dto.UserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResult register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new DuplicateResourceException("Username '" + request.username() + "' is already taken");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email '" + request.email() + "' is already in use");
        }

        var encodedPassword = passwordEncoder.encode(request.password());
        var user = new User(request.username(), request.email(), encodedPassword, Role.USER);
        var savedUser = userRepository.save(user);

        log.info("Registered new user with id: {}", savedUser.getId());

        var token = jwtService.generateToken(savedUser.getId());
        var cookie = jwtService.createTokenCookie(token);

        return new AuthResult(UserDto.from(savedUser), cookie);
    }

    @Transactional(readOnly = true)
    public AuthResult login(LoginRequest request) {
        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        var principal = (CustomUserDetails) authentication.getPrincipal();
        var userId = principal.getId();

        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        log.info("User successfully authenticated with id: {}", userId);

        var token = jwtService.generateToken(userId);
        var cookie = jwtService.createTokenCookie(token);

        return new AuthResult(UserDto.from(user), cookie);
    }

    public ResponseCookie logout() {
        return jwtService.clearTokenCookie();
    }

    @Transactional(readOnly = true)
    public UserDto getCurrentUser() {
        var currentUserId = SecurityUtils.getRequiredCurrentUserId();
        var user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + currentUserId));

        return UserDto.from(user);
    }
}
