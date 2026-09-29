package ru.sber.transport.telemechanic.dto.ewb;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.UUID;

@Schema(description = "Запрос на ввод остатка топлива")
public record EwbLitreageOutRequest(
        @NotNull(message = "Идентификатор ЭПЛ должен быть заполнен")
        @Schema(description = "Идентификатор ЭПЛ",
                requiredMode = Schema.RequiredMode.REQUIRED,
                pattern = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$",
                example = "123e4567-e89b-12d3-a456-426655440000")
        UUID id,
        @NotNull(message = "Текущий остаток топлива должен быть заполнен")
        @PositiveOrZero(message = "Текущий остаток топлива не может быть меньше 0 литров")
        @Schema(description = "Остаток топлива (в литрах)",
                requiredMode = Schema.RequiredMode.REQUIRED,
                pattern = "^[0-9]+$",
                example = "60")
        int value
) {
}
