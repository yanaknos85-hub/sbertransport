package ru.sber.transport.dispatcher.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@NoArgsConstructor
@Setter
@SuperBuilder
@Schema(title = "Информация о параметрах автомобиля", description = "Информация о параметрах автомобиля")
public class CarParameterDto extends NewCarParameterDto {

    @Schema(description = "Идентификатор")
    private UUID id;

}
