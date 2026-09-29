package ru.sber.transport.telemechanic.dto.department;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Schema(description = "Запрос на получение информации о сотруднике по табельному номеру и идентификатору подразделения")
public record SearchEmployeesInfoByDepartmentRequest(
        
        @Size(min = 3, max = 50, message = "Табельный номер должен содержать только от 3 до 50 символов")
        @NotBlank(message = "Табельный номер не может быть пустым")
        @Schema(description = "Табельный номер",
                example = "000000000001",
                minLength = 3,
                maxLength = 50,
                requiredMode = Schema.RequiredMode.REQUIRED)
        String personnelNumber,
        
        @NotBlank(message = "Идентификатор подразделения не может быть пустым")
        @Schema(description = "Идентификатор подразделения",
                type = "string",
                format = "uuid",
                example = "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                minLength = 36,
                maxLength = 36,
                requiredMode = Schema.RequiredMode.REQUIRED)
        UUID departmentId) {
}
