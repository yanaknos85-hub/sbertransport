package ru.sber.transport.telemechanic.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Регион")
public record RegionDto(
        @Schema(description = "Код региона",
                type = "string",
                example = "01",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String code,
        @Schema(description = "Наименование региона и код в скобках",
                type = "string",
                example = "Республика Адыгея (01)",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String title
) {
}
