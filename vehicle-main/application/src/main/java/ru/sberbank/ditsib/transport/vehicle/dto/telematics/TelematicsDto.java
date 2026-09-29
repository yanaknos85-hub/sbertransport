package ru.sberbank.ditsib.transport.vehicle.dto.telematics;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * @author skakun-a
 */
@Schema(title = "Телематика", description = "Телематика")
public record TelematicsDto(

        @NotNull
        @Schema(description = "ID телематики", requiredMode = Schema.RequiredMode.REQUIRED)
        UUID id,

        @NotBlank(message = "IMEI не может быть пустым")
        @Size(min = 1, max = 20)
        @Schema(description = "IME-номер", minLength = 1, maxLength = 20, requiredMode = Schema.RequiredMode.REQUIRED)
        String imei,

        @NotBlank(message = "Наименование Телематики не может быть пустым")
        @Size(min = 1, max = 255)
        @Schema(description = "Наименование", minLength = 1, maxLength = 255, requiredMode = Schema.RequiredMode.REQUIRED)
        String title
) {
}