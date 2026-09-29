package ru.sber.transport.telemechanic.dto.ewb;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

@Schema(description = "Структура для передачи значений одометра для занесения в путевой лист")
public record OdometerValue(
        @NotNull(message = "ИД ЭПЛ должен быть предоставлен")
        @Schema(description = "ИД ЭПЛ")
        UUID id,

        @Positive(message = "Данные о пробеге должны быть больше нуля")
        @NotNull(message = "Данные о пробеге должны быть предоставлены")
        @Schema(description = "Показания одометра (в километрах)")
        Integer value
) {
}
