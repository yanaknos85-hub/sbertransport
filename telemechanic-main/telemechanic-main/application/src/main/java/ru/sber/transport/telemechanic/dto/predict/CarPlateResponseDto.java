package ru.sber.transport.telemechanic.dto.predict;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(name = "Ответ модели car-plates", description = "Ответ модели car-plates")
public record CarPlateResponseDto(
        @Schema(description = "Статус запроса", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        int status,
        @Schema(description = "Статус прохождения проверки", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        boolean result,
        @Schema(description = "Список автомобильных номеров и их размеров", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        List<CarNumberDto> detail) {
}
