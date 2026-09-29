package ru.sberbank.ditsib.transport.vehicle.dto.bodytype;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * @author skakun-a
 */
@Schema(title = "Тип кузова ТС", description = "Тип кузова средства")
public record BodyTypeDto(

        @NotNull
        @Schema(description = "Идентификатор типа кузова ТС", requiredMode = Schema.RequiredMode.REQUIRED)
        UUID id,

        @NotBlank(message = "Наименование типа кузова ТС не может быть пустым")
        @Size(min = 1, max = 255)
        @Schema(description = "Наименование привода ТС", minLength = 1, maxLength = 255, requiredMode = Schema.RequiredMode.REQUIRED)
        String title

) {
}