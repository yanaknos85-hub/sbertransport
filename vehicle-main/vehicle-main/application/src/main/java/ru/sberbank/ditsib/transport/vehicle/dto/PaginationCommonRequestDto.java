package ru.sberbank.ditsib.transport.vehicle.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(title = "Общий запрос с пагинацией ", description = "Структура для получения данных в пагинированном виде")
public record PaginationCommonRequestDto (
        PageSettingDto pageSetting,
        SearchingDto search
) { }
