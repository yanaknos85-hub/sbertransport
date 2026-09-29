package ru.sberbank.ditsib.transport.vehicle.dto.type;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


/**
 * @author skakun-a
 */
@Schema(description = "DTO для создания или изменения Вида ТС")
public record TypeRequestDto(
        @NotBlank(message = "Наименование вида ТС не может быть пустым")
        @Size(min = 1, max = 255)
        @Schema(description = "Наименование вида ТС", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 1, maxLength = 255)
        String title
) {
}
