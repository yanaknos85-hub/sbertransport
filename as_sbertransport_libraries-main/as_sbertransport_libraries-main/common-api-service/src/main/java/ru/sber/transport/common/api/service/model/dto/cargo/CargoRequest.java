package ru.sber.transport.common.api.service.model.dto.cargo;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * CargoRequest
 */

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CargoRequest implements Serializable {
    @JsonProperty("humanReadableId")
    @Schema(description = "Человеко-читаемый идентификатор заявки в АС Сбертранспорт")
    private String humanReadableId;

    @JsonProperty("type")
    @Schema(description = "Тип точки (сбор/доставка)", example = "LOAD | UNLOAD")
    private String type;

    @JsonProperty("cargo")
    @Schema(description = "Массив грузов")
    private List<CargoDetail> cargo;

    @JsonProperty("loaders")
    @Schema(description = "Количество грузчиков")
    private Integer loaders;

    @JsonProperty("comment")
    @Schema(description = "Дополнительная информация к заявке")
    private String comment;
}
