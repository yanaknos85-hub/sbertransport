package ru.sber.transport.telemechanic.dto.ewb;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record TransportDto(
        @NotNull
        @Schema(description = "ID автомобиля")
        UUID id,
        
        @NotBlank
        @Schema(description = "Марка")
        String brand,
        
        @NotBlank
        @Schema(description = "Модель")
        String model,
        
        @NotBlank
        @Schema(description = "Государственный номер")
        String stateNumber,
        
        @NotBlank
        @Schema(description = "Тип транспортного средства")
        String transportType
) {
}
