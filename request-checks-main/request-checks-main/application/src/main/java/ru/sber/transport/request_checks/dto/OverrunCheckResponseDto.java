package ru.sber.transport.request_checks.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(title = "Ответ на проверку суммарного километража", description = "Данные ответа после проверки лимита суммарного километража")
public record OverrunCheckResponseDto(
    @Schema(description = "Комментарий о нарушении", example = "Превышен лимит пробега за месяц")
    String comment,

    @Schema(description = "Суммарная дистанция поездок за месяц в метрах", example = "4500")
    int totalDistance
) {

}
