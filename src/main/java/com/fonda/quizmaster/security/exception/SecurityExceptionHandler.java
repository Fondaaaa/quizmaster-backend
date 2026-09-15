package com.fonda.quizmaster.security.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;

@RestControllerAdvice
public class SecurityExceptionHandler {

    private static final URI BLANK_TYPE = URI.create("about:blank");

    @ExceptionHandler(AuthenticationException.class)
    public ProblemDetail handleAuthenticationException(AuthenticationException ex) {
        var problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED,
                "Full authentication is required to access this resource"
        );
        problemDetail.setTitle("Unauthorized");
        problemDetail.setType(BLANK_TYPE);
        return problemDetail;
    }

    @ExceptionHandler(AuthenticationCredentialsNotFoundException.class)
    public ProblemDetail handleAuthenticationCredentialsNotFoundException(AuthenticationCredentialsNotFoundException ex) {
        var problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED,
                "Authentication credentials not found"
        );
        problemDetail.setTitle("Unauthorized");
        problemDetail.setType(BLANK_TYPE);
        return problemDetail;
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDeniedException(AccessDeniedException ex) {
        var problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.FORBIDDEN,
                "Access is denied"
        );
        problemDetail.setTitle("Forbidden");
        problemDetail.setType(BLANK_TYPE);
        return problemDetail;
    }
}
