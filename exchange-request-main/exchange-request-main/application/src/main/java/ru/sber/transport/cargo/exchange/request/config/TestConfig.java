package ru.sber.transport.cargo.exchange.request.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.jwt.JwtDecoder;

@Configuration
@Profile("test")
public class TestConfig {

    @Bean
    public JwtDecoder jwtDecoder() {
        return token -> null;
    }
}