package ru.sberbank.ditsib.transport.vehicle.dto.indicator;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.Month;

@Schema(title = "Год и месяц внесения показателей", description = "Подсказка по году и месяцу внесения показателей по транспорту")
public record IndicatorDateInfo(
        @Schema(description = "Год внесения показателей", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        @Positive
        int year,
        @Schema(description = "Месяц внесения показателей", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        Month month
) {
}
