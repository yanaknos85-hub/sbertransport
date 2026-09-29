package ru.sber.transport.authentication.business.dto;

import java.time.OffsetDateTime;

/**
 * Data of refresh token.
 *
 * @param value value of token.
 * @param expiration expiration data.
 */
public record RefreshTokenData(
    String value,
    OffsetDateTime expiration
) {
}
