package ru.sberbank.ditsib.transport.vehicle.dto.transmissiontype;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "DTO для создания или изменения Типа трансмиссии ТС")
public record TransmissionTypeRequestDto(
        @NotBlank(message = "Наименование типа трансмиссии ТС не может быть пустым")
        @Size(min = 1, max = 50)
        @Schema(description = "Наименование типа трансмиссии ТС", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 1, maxLength = 50)
        String title
) { }
