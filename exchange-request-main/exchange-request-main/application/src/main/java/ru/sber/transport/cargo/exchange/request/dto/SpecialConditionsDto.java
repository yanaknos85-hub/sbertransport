package ru.sber.transport.cargo.exchange.request.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.UUID;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "SpecialCondition", description = "Особые условия перевозки")
@JsonIgnoreProperties(ignoreUnknown = true)
public class SpecialConditionsDto {

    @Schema(description = "Уникальный идентификатор записи с особыми условиями")
    private UUID id;

    @Schema(description = "Признак, что груз является опасным")
    private Boolean isDangerous;

    @Schema(description = "Класс опасности (от 1 до 9), заполняется только если isDangerous = true", maxLength = 10)
    private String dangerousClass;

    @Schema(description = "Признак необходимости контроля температуры")
    private Boolean hasTemperature;

    @Schema(description = "Минимальная допустимая температура перевозки, °C. Диапазон: от -200 до +50")
    private Short tempMin;

    @Schema(description = "Максимальная допустимая температура перевозки, °C. Диапазон: от -200 до +50")
    private Short tempMax;

    @Schema(description = "Признак, что груз является негабаритным")
    private Boolean isOversized;

    @Schema(description = "Дополнительные условия перевозки в текстовом виде")
    private String otherConditions;
}


