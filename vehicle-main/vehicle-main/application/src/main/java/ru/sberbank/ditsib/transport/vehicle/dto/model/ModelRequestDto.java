package ru.sberbank.ditsib.transport.vehicle.dto.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;


/**
 * @author skakun-a
 */
@Schema(description = "DTO для создания или изменения Модели ТС")
public record ModelRequestDto(
        @NotBlank(message = "Наименование Модели ТС не может быть пустым")
        @Size(min = 1, max = 255)
        @Schema(description = "Наименование Модели ТС", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 1, maxLength = 255)
        String title,
        
        @NotNull(message = "Идентификатор Марки ТС должен быть задан")
        @Schema(description = "Идентификатор Марки ТС", requiredMode = Schema.RequiredMode.REQUIRED)
        UUID brandId
) {
}
