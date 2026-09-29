package ru.sberbank.ditsib.transport.vehicle.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(title = "Контейнер поискового запроса", description = "Структура для передачи данных для поиска по заданным параметрам")
public record SearchingDto (
    @Schema(description = "Поиск по частичному совпадению названия")
    String title
) { }
