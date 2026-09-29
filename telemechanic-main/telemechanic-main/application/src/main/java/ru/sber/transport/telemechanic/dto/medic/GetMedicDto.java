package ru.sber.transport.telemechanic.dto.medic;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(title = "Информация о сотруднике (медике)", description = "Получение информации о медике")
public record GetMedicDto(
        @NotNull
        @Schema(description = "Идентификатор записи")
        UUID id,
        @NotBlank
        @Schema(description = "ФИО медика")
        String fullName,
        @NotBlank
        @Schema(description = "Должность медика")
        String positionName,
        @NotNull
        @Schema(description = "Идентификатор организации")
        UUID organizationId,
        @NotBlank
        @Schema(description = "Наименование организации")
        String organizationName
) {
}
