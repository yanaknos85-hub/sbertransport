package ru.sber.transport.authentication.web.model;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;

/**
 * Данные для второго фактора аутентификации.
 *
 * @param code код аутентификации.
 */
@Schema(title = "Второй фактор", description = "Данные для применения второго фактора аутентификации")
public record CodeData(

        @NotNull
        @Schema(title = "Код", description = "Код второго фактора аутентификации")
        String code
) {
}
