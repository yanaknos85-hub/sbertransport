package ru.sberbank.ditsib.transport.vehicle.dto.drive;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * @author skakun-a
 */
@Schema(title = "Привод ТС", description = "Привод транспортного средства")
public record DriveDto(
        
        @NotNull
        @Schema(description = "Идентификатор привода ТС", requiredMode = Schema.RequiredMode.REQUIRED)
        UUID id,
        
        @NotBlank(message = "Наименование привода ТС не может быть пустым")
        @Size(min = 1, max = 255)
        @Schema(description = "Наименование привода ТС", minLength = 1, maxLength = 255, requiredMode = Schema.RequiredMode.REQUIRED)
        String title
) {
}