package ru.sberbank.ditsib.transport.vehicle.dto.transport.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Обслуживание")
public record ServiceResponseDto(
        @Schema(description = "Межсервисный интервал по пробегу, км", example = "15000", requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 6)
        Integer serviceIntervalMileage,
        @Schema(description = "Межсервисный интервал по времени, дни", example = "12", requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 3)
        Integer serviceIntervalDays,
        @Schema(description = "Допуск по пробегу, км", example = "1000", requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 4)
        Integer serviceAuthorizationMileage,
        @Schema(description = "Допуск по времени, дни", example = "1", requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 3)
        Integer serviceAuthorizationDays
) {
}
