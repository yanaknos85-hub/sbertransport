package ru.sberbank.ditsib.transport.vehicle.dto.indicator;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.Month;

@Schema(title = "Показатели транспорта", description = "Расход топлива")
public record Indicators(
        @Schema(description = "Год внесения показателей", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        @Positive
        @Max(9999)
        int year,
        @Schema(description = "Месяц внесения показателей", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        Month month,
        @Schema(description = "Расход (в литрах)", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        @Positive
        @Max(9999)
        int consumption,
        @Schema(description = "Показатель (в километрах)", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        @Positive
        @Max(999999)
        int value
) {
}
