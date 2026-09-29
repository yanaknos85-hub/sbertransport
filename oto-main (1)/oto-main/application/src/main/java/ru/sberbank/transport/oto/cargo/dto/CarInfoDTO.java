package ru.sberbank.transport.oto.cargo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * Данные по транспортному средству
 */
@Getter
@Setter
@Schema(title = "Данные по транспортному средству", description = "Данные по транспортному средству")
public class CarInfoDTO {
    

    /**
     * Марка ьс
     */
    @Schema(description = "Марка")
    String brandName;

    /**
     * Модель тс
     */
    @Schema(description = "Модель")
    String model;

    /**
     * Цвет тс
     */
    @Schema(description = "Цвет")
    String color;

    /**
     * Государственный номер
     */
    @Schema(description = "Гос. Номер")
    String registrationNumber;
}
