package ru.sberbank.ditsib.transport.request.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@SuperBuilder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "Информация о транспорте", description = "Данные транспорта")
public class VehicleDTO {
    @Schema(description = "Идентификатор")
    private UUID id;

    @Schema(description = "Гос номер")
    private String stateNumber;

    @Schema(description = "Марка автомобиля")
    private String brand;

    @Schema(description = "Наименование модели")
    private String name;

    @Schema(description = "Цвет")
    private String color;

}
