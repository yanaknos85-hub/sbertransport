package ru.sberbank.ditsib.transport.vehicle.dto.brand;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * @author skakun-a
 */
@Schema(title = "Марка ТС", description = "Марка траспортного средства")
public record BrandDto(
        
        @NotNull
        @Schema(description = "Идентификатор марки ТС", requiredMode = Schema.RequiredMode.REQUIRED)
        UUID id,
        
        @NotBlank(message = "Наименование марки ТС не может быть пустым")
        @Size(min = 1, max = 255)
        @Schema(description = "Наименование марки ТС", minLength = 1, maxLength = 255, requiredMode = Schema.RequiredMode.REQUIRED)
        String title
) {
}