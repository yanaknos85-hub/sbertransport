package ru.sberbank.ditsib.transport.vehicle.dto.category;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * @author skakun-a
 */
@Schema(title = "Категория ТС", description = "Категория траспортного средства")
public record CategoryDto(
        
        @NotNull
        @Schema(description = "Идентификатор записи о Категории ТС", requiredMode = Schema.RequiredMode.REQUIRED)
        UUID id,
        
        @NotBlank(message = "Наименование Категории ТС не может быть пустым")
        @Size(min = 1, max =5)
        @Schema(description = "Наименование Категории ТС", minLength = 1, maxLength = 255, requiredMode = Schema.RequiredMode.REQUIRED)
        String category,

        @NotBlank(message = "Наименование Категории ТС не может быть пустым")
        @Size(min = 1, max = 255)
        @Schema(description = "Наименование Категории ТС", minLength = 1, maxLength = 255, requiredMode = Schema.RequiredMode.REQUIRED)
        String title
) {
}