package ru.sberbank.ditsib.transport.vehicle.dto.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ru.sberbank.ditsib.transport.vehicle.dto.brand.BrandDto;

import java.util.UUID;

/**
 * @author skakun-a
 */
@Schema(title = "Модель ТС", description = "Модель траспортного средства")
public record ModelDto(
        
        @NotNull
        @Schema(description = "ID Модели ТС", requiredMode = Schema.RequiredMode.REQUIRED)
        UUID id,
        
        @NotBlank(message = "Наименование модели ТС не может быть пустым")
        @Size(min = 1, max = 255)
        @Schema(description = "Наименование модели ТС", minLength = 1, maxLength = 255, requiredMode = Schema.RequiredMode.REQUIRED)
        String title,
        @NotNull(message = "Марка ТС должна быть задана")
        @Schema(description = "Марка ТС", minLength = 1, maxLength = 255, requiredMode = Schema.RequiredMode.REQUIRED)
        BrandDto brand

) {
}