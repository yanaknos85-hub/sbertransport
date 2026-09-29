package ru.sber.transport.telemechanic.dto.ewb_report;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(name = "EwbSearchDto", title = "Данные для реестра по ЭПЛ")
public record EwbSearchDto(
        @Schema(description = "Результаты поиска")
        List<EwbRegistryResponse> ewbRegistrySearchResult,
        @Schema(description = "Количество элементов в результате поиска")
        int totalElements
) {
}
