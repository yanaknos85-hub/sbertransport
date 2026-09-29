package ru.sber.transport.constants.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

/**
 * Object describes types of transport.
 *
 * @param id ID of type.
 * @param name name of type.
 * @param rusName localized name of type.
 */
@Schema(title = "Тип транспорта", description = "Типы транспорта")
public record TransportTypeDTO(
    @Schema(description = "Идентификатор") UUID id,
    @Schema(description = "Название") String name,
    @Schema(description = "Русское описание для отображения") String rusName) {
}
