package ru.sber.transport.journal.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(title = "Получение заявок на ремонт", description = "Получение заявок на ремонт с пагинацией и сортировкой")
public record JournalDto(
        @Schema(description = "Настройки разделения на страницы")
        PageSetting pageSetting
) {
}
