package ru.sber.transport.dispatcher.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import jakarta.validation.Valid;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "Информация о модели автомобиля", description = "Новые данные модели автомобиля")
public class CarModelDto {

    @Valid
    @Schema(description = "Марка автомобиля")
    private String brand;

    @Valid
    @Schema(description = "Наиенование модели")
    private String name;

    @Valid
    @Schema(description = "Год выпуска")
    private Integer year;

}
