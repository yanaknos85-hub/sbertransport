package ru.sber.transport.constants.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Object describes types of integration.
 *
 * @param name name of type.
 * @param rusName localized name of type.
 */
@Schema(title = "Типы интеграционного взаимодействия для сервисов такси",
    description = "Типы интеграционного взаимодействия для сервисов такси")
public record TaxiIntegrationTypeDTO(
    @Schema(description = "Название") String name,
    @Schema(description = "Русское описание для отображения") String rusName) {

}
