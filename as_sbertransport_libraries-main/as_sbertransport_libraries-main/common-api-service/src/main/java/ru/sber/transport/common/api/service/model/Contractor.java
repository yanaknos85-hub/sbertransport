package ru.sber.transport.common.api.service.model;

import lombok.Builder;
import lombok.Setter;

import java.time.Instant;

@Builder
@Setter
public class Contractor {

    private String token;
    private Instant expirationToken;

    private String refreshToken;
    private Instant expirationRefreshToken;

    private Boolean transferPassword;

    public String getTokenWithPrefix() {
        return "Bearer " + token;
    }

    public String getTokenWithoutPrefix() {
        return token;
    }

    public String getRefreshTokenWithPrefix() {
        return "Token " + refreshToken;
    }

    public Instant getExpirationToken() {
        return expirationToken;
    }

    public Instant getExpirationRefreshToken() {
        return expirationRefreshToken;
    }

}

