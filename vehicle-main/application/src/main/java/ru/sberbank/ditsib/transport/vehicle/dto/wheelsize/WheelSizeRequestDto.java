package ru.sberbank.ditsib.transport.vehicle.dto.wheelsize;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "DTO для создания или изменения Размер колеса ТС")
public record WheelSizeRequestDto(
        @NotBlank(message = "Наименование размер колеса ТС не может быть пустым")
        @Size(min = 1, max = 50)
        @Schema(description = "Наименование размера колеса ТС", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 1, maxLength = 50)
        String title
) { }
