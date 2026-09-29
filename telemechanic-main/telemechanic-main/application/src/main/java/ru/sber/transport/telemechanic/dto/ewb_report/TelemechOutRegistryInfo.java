package ru.sber.transport.telemechanic.dto.ewb_report;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(name = "TelemechOutRegistryDto", title = "Данные для таблицы реестра по телемеханику на выезде",
        description = "Данные по телемеханику на выезде для ответа формирования реестар ЭПЛ")
public record TelemechOutRegistryInfo(
        @Size(max = 255, message = "Не более 255 символов")
        @Schema(description = "Наименование организации",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                example = "ПАО Сбер",
                nullable = true,
                maximum = "255")
        String organizationName,
        
        @Size(max = 255, message = "Не более 255 символов")
        @Schema(description = "Наименование подразделения",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                example = "Группа разработки",
                nullable = true,
                maximum = "255")
        String departmentName,
        
        @Size(max = 255, message = "Не более 255 символов")
        @Schema(description = "ФИО",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                example = "Иванов Иван Иванович",
                nullable = true,
                maximum = "255")
        String fullName,
        
        @Size(max = 255, message = "Не более 255 символов")
        @Schema(description = "Табельный номер",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                example = "11122233445",
                nullable = true,
                maximum = "255")
        String personnelNumber
) {
}
