package ru.sber.transport.telemechanic.enumerate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(name = "MedicRequestSortOption",
        title = "Настройка сортировки реестра медицинских осмотров",
        description = "Настройка сортировки реестра медицинских осмотров")
public enum MedicRequestSortOption {
    
    @Schema(description = "Номер медицинского осмотра")
    MEDIC_REQUEST_HUMAN_READABLE_ID("m1_0.human_readable_id"),
    @Schema(description = "Наименование организации")
    MEDIC_ORGANIZATION_NAME("o1_0.official_name"),
    @Schema(description = "Идентификатор ewb")
    EWB_ID("e1_0.id");
    private final String sqlValue;
}
