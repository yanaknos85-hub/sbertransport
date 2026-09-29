package ru.sberbank.ditsib.transport.reports.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(title = "Данные по транспортному средству", description = "Данные по транспортному средству")
public class CarInfoDTO {
    
    @Schema(description = "Марка")
    String brandName;
    
    @Schema(description = "Модель")
    String model;
    
    @Schema(description = "Цвет")
    String color;
    
    @Schema(description = "Гос. Номер")
    String registrationNumber;
}
