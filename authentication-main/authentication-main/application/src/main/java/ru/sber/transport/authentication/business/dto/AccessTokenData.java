package ru.sber.transport.authentication.business.dto;

import java.time.OffsetDateTime;

/**
 * Данные токена доступа.
 *
 * @param value значение токена.
 * @param expiration время эксирации.
 */
public record AccessTokenData(
        String value,
        OffsetDateTime expiration
) {
}
