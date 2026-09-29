package ru.sber.transport.telemechanic.dto.driver;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Schema(name = "DriverSearchDto", description = "Данные водителя")
public record DriverSearchDto(
        @NotNull(message = "Идентификатор записи сотрудника не может отсутствовать")
        @Schema(description = "Идентификатор записи сотрудника",
                requiredMode = Schema.RequiredMode.REQUIRED,
                type = "string",
                format = "uuid",
                example = "ed518f6f-f2d1-4582-b49b-edc4b5d1959f",
                minLength = 36, maxLength = 36)
        UUID id,
        
        @NotBlank(message = "Табельный номер сотрудника не может быть пустым")
        @Schema(description = "Табельный номер водителя",
                requiredMode = Schema.RequiredMode.REQUIRED,
                type = "string",
                example = "21324245",
                maxLength = 255)
        String personnelNumber,
        
        @NotBlank(message = "ФИО водителя не может быть пустым")
        @Schema(description = "ФИО водителя",
                requiredMode = Schema.RequiredMode.REQUIRED,
                example = "Иванов Иван Иванович")
        String fullName,
        
        @NotBlank(message = "Наименование организации водителя не может быть пустым")
        @Schema(description = "Наименование организации",
                requiredMode = Schema.RequiredMode.REQUIRED,
                type = "string",
                example = "Байкальский Банк",
                maxLength = 255)
        String organizationName,
        
        @NotBlank(message = "Наименование подразделения водителя не может быть пустым")
        @Schema(description = "Наименование подразделения",
                requiredMode = Schema.RequiredMode.REQUIRED,
                type = "string",
                example = "КИЦ Волгоградский",
                maxLength = 255)
        String departmentName,
        
        @NotBlank(message = "ИНН водителя не может быть пустым")
        @Size(min = 5, max = 12, message = "ИНН должен содержать от 5 до 12 символов")
        @Pattern(regexp = "^\\d{5,12}$", message = "ИНН не прошел проверку")
        @Schema(description = "ИНН",
                requiredMode = Schema.RequiredMode.REQUIRED,
                type = "string",
                example = "111111111111")
        String tin,
        
        @Size(min = 11, max = 14, message = "СНИЛС должен содержать от 11 до 14 символов")
        @Schema(description = "СНИЛС",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                example = "111-111-111 11")
        String snils
) {
}
