package ru.sber.transport.authentication.business.dto;

import java.time.OffsetDateTime;

/**
 * Данные токенов вошедшего пользователя.
 *
 * @param accessToken токен доступа.
 * @param refreshToken токен обновления.
 * @param transferPassword пароль транспортный.
 * @param authType тип авторизации.
 * @param accessExpiration время истечения токена доступа.
 * @param refreshExpiration время истечения токена обновления.
 */
public record Token(
        String accessToken,
        String refreshToken,
        boolean transferPassword,
        AuthType authType,
        OffsetDateTime accessExpiration,
        OffsetDateTime refreshExpiration
) {
}
