package ru.sber.transport.dispatcher.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import jakarta.validation.Valid;

/**
 * DTO - Информация о параметрах автомобиля
 */
@NoArgsConstructor
@Getter
@Setter
@SuperBuilder
@Schema(title = "Информация о параметрах автомобиля", description = "Новые данные параметра автомобиля")
public class NewCarParameterDto {
    
    @Valid
    @Schema(description = "Цвет")
    private String color;
    
    @Valid
    @Schema(description = "Пробег")
    private Double mileage;
    
    @Valid
    @Schema(description = "Год выпуска")
    private Integer year;
 
    @Valid
    @Schema(description = "Параметры автомобиля")
    private CarModelDto model;
    
}
