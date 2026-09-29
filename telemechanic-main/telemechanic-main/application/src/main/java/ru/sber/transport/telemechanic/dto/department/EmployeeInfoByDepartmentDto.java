package ru.sber.transport.telemechanic.dto.department;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "Информация о сотруднике подразделения")
public record EmployeeInfoByDepartmentDto(
        @NotNull(message = "Идентификатор сотрудника не может быть пустым")
        @Schema(description = "Идентификатор сотрудника",
                type = "string",
                format = "uuid",
                example = "82862453-539d-4569-865d-582761565105",
                minLength = 36, maxLength = 36,
                requiredMode = Schema.RequiredMode.REQUIRED)
        UUID id,
        
        @NotBlank(message = "Табельный номер не может быть пустым")
        @Schema(description = "Табельный номер",
                type = "string",
                example = "21324245",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String personnelNumber,
        
        @NotBlank(message = "ФИО не может быть пустым")
        @Schema(description = "ФИО",
                type = "string",
                example = "Иванов Иван Иванович",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String fullName,
        
        @NotBlank(message = "Должность не может быть пустой")
        @Schema(description = "Должность",
                type = "string",
                example = "Фельдшер",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String positionName,
        
        @Schema(description = "Табельный номер",
                type = "string",
                example = "1234567890",
                maxLength = 12,
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String tin
) {
}
