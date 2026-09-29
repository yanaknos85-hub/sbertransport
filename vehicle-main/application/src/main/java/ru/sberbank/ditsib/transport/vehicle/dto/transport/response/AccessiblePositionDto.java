package ru.sberbank.ditsib.transport.vehicle.dto.transport.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

import java.util.UUID;

@Schema(description = "Модель справочника должностей для закрепления")
public record AccessiblePositionDto(
        @Schema(description = "Идентификатор должности", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        @NotNull
        UUID id,

        @Schema(description = "Наименование должности", example = "ТОН подразделения безопасности", maxLength = 255)
        @NotNull
        @Length(max = 255)
        String title
) {
}
