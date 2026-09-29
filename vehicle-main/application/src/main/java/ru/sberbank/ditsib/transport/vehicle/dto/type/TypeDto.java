package ru.sberbank.ditsib.transport.vehicle.dto.type;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * @author skakun-a
 */
@Schema(title = "Наименование ТС", description = "Наименование ТС")
public record TypeDto(
        
        @NotNull
        @Schema(description = "ID вида", requiredMode = Schema.RequiredMode.REQUIRED)
        UUID id,
        
        @NotBlank(message = "Наименование вида ТС не может быть пустым")
        @Size(min = 1, max = 255)
        @Schema(description = "Наименование вида ТС", minLength = 1, maxLength = 255, requiredMode = Schema.RequiredMode.REQUIRED)
        String title
) {
}