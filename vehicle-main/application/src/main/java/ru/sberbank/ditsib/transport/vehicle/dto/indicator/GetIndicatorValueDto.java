package ru.sberbank.ditsib.transport.vehicle.dto.indicator;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(title = "Получение значения одометра", description = "Показание одометра")
public record GetIndicatorValueDto(
        @NotNull
        @Positive
        @Max(999999)
        @Schema(description = "Значение одометра", requiredMode = Schema.RequiredMode.REQUIRED)
        int value
) {
}
