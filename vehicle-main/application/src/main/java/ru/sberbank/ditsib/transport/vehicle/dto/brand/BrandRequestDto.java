package ru.sberbank.ditsib.transport.vehicle.dto.brand;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


/**
 * @author skakun-a
 */
@Schema(description = "DTO для создания или изменения Марки ТС")
public record BrandRequestDto(
        @NotBlank(message = "Наименование марки ТС не может быть пустым")
        @Size(min = 1, max = 255)
        @Schema(description = "Наименование", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 1, maxLength = 255)
        String title
) {
}
