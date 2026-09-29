package ru.sberbank.ditsib.transport.vehicle.dto.telematics;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;


/**
 * @author skakun-a
 */
@Schema(description = "DTO для создания или изменения Телематики")
public record TelematicsRequestDto(
        @NotBlank(message = "Наименование Телематики не может быть пустым")
        @Size(min = 1, max = 20)
        @Pattern(regexp = "\\d+")
        @Schema(description = "IMEI", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 1, maxLength = 20)
        String imei,

        @NotBlank(message = "Наименование Категории ТС не может быть пустым")
        @Size(min = 1, max = 255)
        @Schema(description = "Наименование", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 1, maxLength = 255)
        String title
) {
}
