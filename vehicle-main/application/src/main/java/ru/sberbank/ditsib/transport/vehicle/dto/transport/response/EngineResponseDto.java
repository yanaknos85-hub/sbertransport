package ru.sberbank.ditsib.transport.vehicle.dto.transport.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Двигатель")
public record EngineResponseDto(
        @Schema(description = "Тип двигателя", example = "Бензин")
        String engineType,
        @Schema(description = "Объем двигателя", example = "2500", requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 5)
        Integer engineCapacity,
        @Schema(description = "Мощность ЛС", example = "215.77", requiredMode = Schema.RequiredMode.REQUIRED)
        BigDecimal enginePower,
        @Schema(description = "Тип топлива", example = "АИ-80", maxLength = 255)
        String fuelType,
        @Schema(description = "Городской расход", example = "10.99", requiredMode = Schema.RequiredMode.NOT_REQUIRED,
        minimum = "1", maximum = "999.99")
        BigDecimal cityConsumptionRate,
        @Schema(description = "Загородный расход", example = "10.99", requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                minimum = "1", maximum = "999.99")
        BigDecimal countryConsumptionRate,
        @Schema(description = "Смешанный расход", example = "10.99", requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                minimum = "1", maximum = "999.99")
        BigDecimal hybridConsumptionRate
) {
}
