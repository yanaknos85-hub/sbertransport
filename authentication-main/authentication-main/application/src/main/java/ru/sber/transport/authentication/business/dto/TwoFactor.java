package ru.sber.transport.authentication.business.dto;

import java.time.LocalDateTime;

/**
 * Объект, описывающий второй фактор авторизации.
 *
 * @param token токен для запроса.
 * @param code код подтверждения.
 * @param expiration время экспирации кода.
 */
public record TwoFactor(
        String token,
        String code,
        LocalDateTime expiration
) {
}
