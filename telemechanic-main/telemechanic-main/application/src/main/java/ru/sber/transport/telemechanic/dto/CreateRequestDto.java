package ru.sber.transport.telemechanic.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(title = "Создание заявки на выход на линию")
public record CreateRequestDto(
        @NotNull(message = "Идентификатор транспорта не может быть пустым")
        @Schema(description = "Идентификатор транспорта",
                requiredMode = Schema.RequiredMode.REQUIRED)
        UUID transportId
) {
}
