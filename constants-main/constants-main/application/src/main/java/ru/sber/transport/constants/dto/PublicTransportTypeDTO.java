package ru.sber.transport.constants.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * Object describes types of public transport.
 *
 * @param name name of type.
 * @param rusName localized name of type.
 * @param publicCompensationType type of compensation.
 */
@Schema(title = "Доступные типы общественного транспорта", description = "Доступные типы общественного транспорта")
public record PublicTransportTypeDTO(
    @NotNull
    @Schema(description = "Наименование константы") String name,

    @Schema(description = "Русскоязычное наименование для фронта") String rusName,

    @Schema(description = "Тип компенсации") String publicCompensationType
) {
}