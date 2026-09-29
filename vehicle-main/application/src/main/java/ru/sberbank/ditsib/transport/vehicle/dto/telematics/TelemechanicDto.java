package ru.sberbank.ditsib.transport.vehicle.dto.telematics;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "Данные телемеханика")
public record TelemechanicDto(

        @NotNull
        @Schema(description = "ID телемеханика", requiredMode = Schema.RequiredMode.REQUIRED)
        UUID id,

        @NotBlank
        @Schema(description = "Таб.номер телемеханика", requiredMode = Schema.RequiredMode.REQUIRED)
        String personnelNumber,

        @NotBlank
        @Schema(description = "ФИО телемеханика", requiredMode = Schema.RequiredMode.REQUIRED)
        String fullName,

        @NotNull
        @Schema(description = "ID организации", requiredMode = Schema.RequiredMode.REQUIRED)
        UUID organizationId,

        @NotBlank
        @Schema(description = "Организация", requiredMode = Schema.RequiredMode.REQUIRED)
        String organizationName,

        @NotNull
        @Schema(description = "ID подразделения", requiredMode = Schema.RequiredMode.REQUIRED)
        UUID departmentId,

        @NotBlank
        @Schema(description = "Подразделение", requiredMode = Schema.RequiredMode.REQUIRED)
        String departmentName

) {
}
