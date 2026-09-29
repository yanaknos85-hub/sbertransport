package ru.sberbank.ditsib.transport.vehicle.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Запись справочника в формате ключ-значение")
public record FilterValue(
        @Schema(description = "Ключ, уникальный идентификатор", requiredMode = Schema.RequiredMode.REQUIRED)
        UUID id,
        @Schema(description = "Название", requiredMode = Schema.RequiredMode.REQUIRED)
        String title
) {
}
