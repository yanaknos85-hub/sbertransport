package ru.sberbank.ditsib.transport.vehicle.dto.bodytype;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "DTO для создания или изменения Типа кузова ТС")
public record BodyTypeRequestDto(
        @NotBlank(message = "Наименование типа кузова ТС не может быть пустым")
        @Size(min = 1, max = 255)
        @Schema(description = "Наименование типа кузова ТС", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 1, maxLength = 255)
        String title
) { }
