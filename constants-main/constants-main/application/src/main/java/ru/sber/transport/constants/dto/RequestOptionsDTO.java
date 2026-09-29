package ru.sber.transport.constants.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Object describes types of request options.
 *
 * @param name name of type.
 * @param rusName localized name of type.
 */
@Schema(title = "Доступные опции", description = "Доступные опции при заказе")
public record RequestOptionsDTO(
    @Schema(description = "Название") String name,
    @Schema(description = "Русское описание для отображения") String rusName) {

}
