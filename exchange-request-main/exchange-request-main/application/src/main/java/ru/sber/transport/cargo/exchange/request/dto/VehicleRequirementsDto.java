package ru.sber.transport.cargo.exchange.request.dto;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "VehicleRequirements", description = "Требования к транспортному средству")
@JsonIgnoreProperties(ignoreUnknown = true)
public class VehicleRequirementsDto {

    @Schema(description = "Уникальный идентификатор записи с требованиями к транспорту")
    private UUID id;

    @Schema(
            description = "Тип загрузки: top, side, rear",
            allowableValues = {"top", "side", "rear"},
            maxLength = 50
    )
    private String loadType;

    @Schema(
            description = "Тип выгрузки: top, side, rear",
            allowableValues = {"top", "side", "rear"},
            maxLength = 50
    )
    private String unloadType;

    @Schema(
            description = "Требуемый объём кузова в кубометрах. Диапазон: от 1 до 90 м³",
            minimum = "1.0",
            maximum = "90.0"
    )
    private BigDecimal capacityM3;

    @Schema(
            description = "Требуемая грузоподъёмность в тоннах. Диапазон: от 0.001 до 20.0 тонн",
            minimum = "0.001",
            maximum = "20.0"
    )
    private BigDecimal loadCapacity;

    @Schema(description = "Запрет на догрузку: true — без догрузки, false — можно догружать")
    private boolean noAdditionalLoad;

    @Schema(description = "Тип кузова (например, тент, рефрижератор). Ссылка на справочник vehicle_body_types.id")
    private List<String> vehicleBodyType;

    @Schema(description = "Дополнительные опции транспорта (манипулятор, подогрев и т.д.). Ссылка на справочник vehicle_extra_features.id")
    private List<String> vehicleExtraFeatures;

    @Schema(description = "Дополнительный комментарий к требованиям к транспорту")
    private String comment;
}

