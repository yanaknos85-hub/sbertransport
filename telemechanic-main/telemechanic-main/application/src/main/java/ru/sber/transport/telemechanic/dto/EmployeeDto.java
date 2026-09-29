package ru.sber.transport.telemechanic.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.With;

import java.util.UUID;

@With
@Schema(title = "Информация о сотруднике", description = "Данные сотрудника")
public record EmployeeDto(
        @NotNull
        @Schema(description = "Идентификатор", requiredMode = Schema.RequiredMode.REQUIRED)
        UUID id,
        @Schema(description = "Имя")
        String firstName,
        @Schema(description = "Фамилия")
        String lastName,
        @Schema(description = "Отчество")
        String patronymic,
        @Schema(description = "Табельный номер")
        String personnelNumber,
        UUID organizationId,
        @Schema(description = "Служебное название организации")
        String organizationOfficialName
) {

}
