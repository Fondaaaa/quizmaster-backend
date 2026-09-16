package com.fonda.quizmaster.auth.dto;

import com.fonda.quizmaster.user.dto.UserDto;
import org.springframework.http.ResponseCookie;

public record AuthResult(
        UserDto user,
        ResponseCookie cookie
) {
}
