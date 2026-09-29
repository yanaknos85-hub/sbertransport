package ru.sber.transport.request_checks.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(title = "Ошибка", description = "Данные об ошибке")
public record ErrorResponse(
    @Schema(description = "Сообщение об ошибке", requiredMode = Schema.RequiredMode.REQUIRED)
    String message
) {

}