package ru.sber.transport.constants.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Object describes types of personal car owning.
 *
 * @param name name of type.
 * @param rusName localized name of type.
 */
@Schema(title = "Информация о собственнике ТС", description = "Информация о собственнике ТС")
public record PersonalCarOwnerInfoEnumDTO(
    @Schema(description = "Название") String name,
    @Schema(description = "Русское описание для отображения") String rusName) {
}
