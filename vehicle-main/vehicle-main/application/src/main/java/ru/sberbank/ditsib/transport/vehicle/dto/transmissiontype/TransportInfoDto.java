package ru.sberbank.ditsib.transport.vehicle.dto.transmissiontype;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import ru.sberbank.ditsib.transport.vehicle.constants.TransportStatus;

import java.util.UUID;

@Schema(description = "Информация о транспортном средстве")
public record TransportInfoDto(
        
        @Schema(description = "Идентификатор транспортного средства")
        UUID id,
        
        @Size(min = 8, max = 9)
        @Schema(description = "Государственный номер транспортного средства", minLength = 8, maxLength = 9, example = "A777AA77")
        String stateNumber,
        
        @Schema(description = "Статус транспортного средства")
        TransportStatus status,
        
        @Schema(description = "Марка транспортного средства")
        String brand,
        
        @Schema(description = "Модель транспортного средства")
        String model,
        
        @Schema(description = "Тип транспортного средства")
        String transportType
) {
}
