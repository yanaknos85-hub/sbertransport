package ru.sber.transport.contractor.dto.internal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(title = "Данные для создание связи контрагентов", description = "Данные для создание связи контрагентов между инстансами")
public record LinkRequestDTO(
        @NotNull
        @Schema(description = "ОГРН, 13 знаков")
        @Size(min = 13, max = 15)
        String msrn,

        @NotNull
        @Schema(description = "ИНН 10 знаков")
        @Size(min = 10, max = 12)
        String tin,

        @NotNull
        @Schema(description = "Контактная почта контрагента")
        String contactPersonEmail,

        @NotNull
        @Schema(description = "Логин", minLength = 1)
        String login,

        @NotBlank
        @Schema(description = "Пароль", minLength = 1)
        String password
) {
}