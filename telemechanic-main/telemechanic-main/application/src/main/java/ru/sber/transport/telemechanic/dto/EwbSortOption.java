package ru.sber.transport.telemechanic.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Schema(name = "EwbSortSettingDto", title = "Настройка сортировки реестра ЭПЛ", description = "Настройка сортировки реестра ЭПЛ")
public enum EwbSortOption {
    @Schema(description = "ID путевого листа")
    HUMAN_READABLE_ID("ID путевого листа", "ewb.human_readable_id"),
    @Schema(description = "Название организации водителя")
    ORGANIZATION_NAME("Название организации водителя", "org.official_name");
    
    private final String description;
    private final String sqlValue;
}
