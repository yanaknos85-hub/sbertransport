package ru.sberbank.ditsib.transport.vehicle.dto.enginetype;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * @author skakun-a
 */
@Schema(title = "Тип двигателя ТС", description = "Тип двигателя транспортного средства")
public record EngineTypeDto(
        
        @NotNull
        @Schema(description = "Тип двигателя ТС", requiredMode = Schema.RequiredMode.REQUIRED)
        UUID id,
        
        @NotBlank(message = "Наименование типа двигятеля ТС не может быть пустым")
        @Size(min = 1, max = 255)
        @Schema(description = "Наименование типа двигателя ТС", minLength = 1, maxLength = 255, requiredMode = Schema.RequiredMode.REQUIRED)
        String title
) {
}