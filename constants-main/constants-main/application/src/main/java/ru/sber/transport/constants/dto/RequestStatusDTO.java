package ru.sber.transport.constants.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Object describes status of request.
 *
 * @param name name of type.
 * @param rusName localized name of type.
 * @param editable user can edit request.
 * @param approvable user should approve request.
 * @param cancelable user can cancel request.
 * @param finalStatus is status is terminal.
 * @param color color for displaying.
 */
@Schema(title = "Статусы заявок", description = "Статусы заявок")
public record RequestStatusDTO(
    @Schema(description = "Название") String name,
    @Schema(description = "Русское описание для отображения") String rusName,
    @Schema(description = "Флаг возможности редактирования") boolean editable,
    @Schema(description = "Флаг возможности согласования") boolean approvable,
    @Schema(description = "Флаг возможности отмены") boolean cancelable,
    @Schema(description = "Флаг финального статуса") boolean finalStatus,
    @Schema(description = "Цвет для отрисовки статуса") String color) {

}
