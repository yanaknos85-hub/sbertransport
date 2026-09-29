package ru.sber.transport.telemechanic.dto.predict;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "Автомобильный номер и его размер", description = "Данные об автомобильном номере и его размере")
public record CarNumberDto(@JsonProperty("car_number")
                           @Schema(description = "Автомобильный номер", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
                           String carNumber,
                           @Schema(description = "Размер номера", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
                           @JsonProperty("plate_square")
                           Integer plateSquare) {
}
    