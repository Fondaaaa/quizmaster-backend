package com.fonda.quizmaster.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "security.jwt")
public class JwtProperties {

    private String secret;
    private Duration expiration = Duration.ofDays(1);
    private String cookieName = "access_token";
    private boolean cookieSecure = false;
    private String cookieSameSite = "Lax";
}
