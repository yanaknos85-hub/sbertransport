package ru.sber.transport.telemechanic.dto.ewb;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

@Schema(description = "Запрос на закрытие ЭПЛ")
public record EwbCloseRequest(
        @NotNull(message = "ИД ЭПЛ должен быть предоставлен")
        @Schema(description = "ИД ЭПЛ")
        UUID id,
        @Positive(message = "Данные о пробеге должны быть больше нуля")
        @NotNull(message = "Данные о пробеге должны быть предоставлены")
        @Schema(description = "Показания одометра (в километрах)")
        Integer value,
        @Positive(message = "Данные по остаткам топлива должны быть больше нуля")
        @NotNull(message = "Данные по остаткам топлива должны быть предоставлены")
        @Schema(description = "Остаток топлива (в литрах)",
                requiredMode = Schema.RequiredMode.REQUIRED,
                pattern = "^[0-9]+$",
                example = "35")
        Integer fuelLitreage
) {
}
