package ru.sber.transport.telemechanic.dto.ewb_report;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "DriverRegistryInfo", title = "Данные для реестра по водителю",
        description = "Данные по водителю для ответа формирования реестар ЭПЛ")
public record DriverRegistryInfo(
        @NotBlank(message = "ФИО водителя не должно быть пустым")
        @Size(max = 255, message = "Размер ФИО водителя должен быть не больше 255 символов")
        @Schema(description = "ФИО водителя",
                requiredMode = Schema.RequiredMode.REQUIRED,
                example = "Ромашин Роман Ромашкович",
                maximum = "255")
        String fullName,
        
        @Size(max = 255, message = "Размер табельного номера водителя должен быть не больше 255 символов")
        @Schema(description = "Табельный номер водителя",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                example = "11122233445",
                nullable = true,
                maximum = "255")
        String personnelNumber,
        
        @NotBlank(message = "Наименование организации не должно быть пустым")
        @Size(max = 255, message = "Размер наименования организации должен быть не больше 255 символов")
        @Schema(description = "Наименование организации",
                requiredMode = Schema.RequiredMode.REQUIRED,
                example = "ЦА",
                maximum = "255")
        String organizationName,
        
        @NotBlank(message = "Наименование подразделение не должно быть пустым")
        @Size(max = 255, message = "Размер наименования подразделения должен быть не больше 255 символов")
        @Schema(description = "Наименование подразделения",
                requiredMode = Schema.RequiredMode.REQUIRED,
                example = "Группа разработки",
                maximum = "255")
        String departmentName
) {
}