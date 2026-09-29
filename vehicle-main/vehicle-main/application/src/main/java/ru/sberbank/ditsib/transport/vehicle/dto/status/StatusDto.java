package ru.sberbank.ditsib.transport.vehicle.dto.status;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * @author skakun-a
 */
@Schema(title = "Статус ТС", description = "Статус ТС")
public record StatusDto(
        
        @NotNull
        @Schema(description = "ID статуса", requiredMode = Schema.RequiredMode.REQUIRED)
        UUID id,
        
        @NotBlank(message = "Наименование статуса ТС не может быть пустым")
        @Size(min = 1, max = 255)
        @Schema(description = "Наименование статуса ТС", minLength = 1, maxLength = 255, requiredMode = Schema.RequiredMode.REQUIRED)
        String title
) {
}