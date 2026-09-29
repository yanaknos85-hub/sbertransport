package ru.sber.transport.notifications.dto.contractor;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@Schema(description = "Данные об автомобиле")
public class VehicleDTO {

    @Schema(description = "Брэнд")
    private String brand;

    @Schema(description = "Модель")
    private String model;

    @Schema(description = "Номер")
    private String stateNumber;

    @Schema(description = "Цвет")
    private String color;

    @Schema(description = "Собранная информация по авто")
    private String carInfo;

}
