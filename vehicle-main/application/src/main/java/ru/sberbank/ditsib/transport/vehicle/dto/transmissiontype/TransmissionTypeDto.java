package ru.sberbank.ditsib.transport.vehicle.dto.transmissiontype;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * @author skakun-a
 */
@Schema(title = "Тип трасмиссии ТС", description = "Тип трасмиссии средства")
public record TransmissionTypeDto(

        @NotNull
        @Schema(description = "Идентификатор типа трасмиссии ТС", requiredMode = Schema.RequiredMode.REQUIRED)
        UUID id,

        @NotBlank(message = "Наименование типа трасмиссии ТС не может быть пустым")
        @Size(min = 1, max = 50)
        @Schema(description = "Наименование типа трасмиссии ТС", minLength = 1, maxLength = 50, requiredMode = Schema.RequiredMode.REQUIRED)
        String title

) {
}