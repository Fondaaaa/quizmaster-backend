package com.fonda.quizmaster.security.jwt;

import com.fonda.quizmaster.security.user.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        var tokenOpt = jwtService.extractTokenFromCookie(request);

        if (tokenOpt.isPresent()) {
            var token = tokenOpt.get();
            if (jwtService.validateToken(token)) {
                try {
                    var userId = jwtService.extractUserId(token);
                    if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                        var userDetails = customUserDetailsService.loadUserById(userId);

                        var authentication = new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                        var context = SecurityContextHolder.createEmptyContext();
                        context.setAuthentication(authentication);
                        SecurityContextHolder.setContext(context);
                    }
                } catch (UsernameNotFoundException | DisabledException e) {
                    log.debug("User could not be authenticated from JWT: {}", e.getMessage());
                } catch (Exception e) {
                    log.debug("Unexpected error during JWT authentication: {}", e.getMessage());
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
