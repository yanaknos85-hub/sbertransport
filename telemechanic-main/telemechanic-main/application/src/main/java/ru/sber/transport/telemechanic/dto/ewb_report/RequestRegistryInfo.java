package ru.sber.transport.telemechanic.dto.ewb_report;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(name = "RequestRegistryDto", title = "Данные для таблицы реестра по заявке телемеханика",
        description = "Данные по заявке телемеханика для ответа формирования реестар ЭПЛ")
public record RequestRegistryInfo(
        @Size(max = 36, message = "Не более 36 символов")
        @Schema(description = "Номер заявки",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                example = "TM-0001-0000001",
                nullable = true,
                maximum = "36")
        String humanReadableId
) {
}
