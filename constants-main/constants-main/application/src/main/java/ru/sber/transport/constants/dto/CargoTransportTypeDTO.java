package ru.sber.transport.constants.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * Object describes types of cargo type.
 *
 * @param name name of type.
 * @param rusName localized name of type.
 */
@Schema(title = "Доступные типы грузового транспорта", description = "Доступные типы грузового транспорта")
public record CargoTransportTypeDTO(
    @NotNull @Schema(description = "Наименование константы") String name,
    @Schema(description = "Русскоязычное наименование для фронта") String rusName
) {

}
