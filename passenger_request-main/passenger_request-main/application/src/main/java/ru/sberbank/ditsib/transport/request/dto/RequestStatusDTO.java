package ru.sberbank.ditsib.transport.request.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@Schema(title = "Статусы заявок", description = "Статусы заявок")
public class RequestStatusDTO {

    /**
     * Имя
     */
    @Schema(description = "Название")
    private final String name;

    /**
     * Русское описание
     */
    @Schema(description = "Русское описание для отображения")
    private final String rusName;

    @Schema(description = "Флаг финального статуса")
    private final boolean finalStatus;

    @Schema(description = "Цвет для отрисовки статуса")
    private final String color;
}
