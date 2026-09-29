package ru.sberbank.ditsib.transport.reports.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(title = "Результат запроса выгрузки")
public record TaskResultDto(
        @Schema(title = "УРЛ для запроса данных")
        String url,
        @Schema(title = "Статус запуска")
        boolean started
) { }
