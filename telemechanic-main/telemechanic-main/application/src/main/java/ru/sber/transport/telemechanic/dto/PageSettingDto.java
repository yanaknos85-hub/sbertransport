package ru.sber.transport.telemechanic.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(title = "Настройки пагинации", description = "Структура для получения данных в пагинированном виде")
public record PageSettingDto(
    @Schema(description = "Номер страницы")
    int page,
    
    @Schema(description = "Количество элементов на странице")
    int size
) { }