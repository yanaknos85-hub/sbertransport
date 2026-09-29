package ru.sber.transport.telemechanic.dto.ewb_report;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(name = "TelemechInRegistryDto", title = "Данные для таблицы реестра по телемеханику на въезде",
        description = "Данные по телемеханику на въезде для ответа формирования реестар ЭПЛ")
public record TelemechInRegistryInfo(
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
                example = "123456789",
                nullable = true,
                maximum = "255")
        String personnelNumber
) {
}
