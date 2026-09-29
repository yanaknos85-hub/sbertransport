package ru.sberbank.ditsib.transport.vehicle.dto.category;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;


/**
 * @author skakun-a
 */
@Schema(description = "DTO для создания или изменения Категории ТС")
public record CategoryRequestDto(
        @NotBlank(message = "Наименование Категории ТС не может быть пустым")
        @Size(min = 1, max = 5)
        @Schema(description = "Категория", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 1, maxLength = 255)
        @Pattern(regexp = "^[a-zA-Z0-9]+$")
        String category,
        
        @NotBlank(message = "Наименование Категории ТС не может быть пустым")
        @Size(min = 1, max = 255)
        @Schema(description = "Наименование", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 1, maxLength = 255)
        String title
) {
}
