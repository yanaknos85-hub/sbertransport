package ru.sberbank.ditsib.transport.vehicle.dto.drive;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


/**
 * @author skakun-a
 */
@Schema(description = "DTO для создания или изменения Привода ТС")
public record DriveRequestDto(
        @NotBlank(message = "Наименование привода ТС не может быть пустым")
        @Size(min = 1, max = 255)
        @Schema(description = "Наименование привода ТС", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 1, maxLength = 255)
        String title
) {
}
