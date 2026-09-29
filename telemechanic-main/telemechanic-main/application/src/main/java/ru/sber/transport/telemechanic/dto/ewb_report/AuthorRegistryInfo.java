package ru.sber.transport.telemechanic.dto.ewb_report;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(name = "AuthorRegistryInfo", title = "Данные об авторе ЭПЛ для реестра",
        description = "Данные по автору ЭПЛ для ответа формирования реестар ЭПЛ")
public record AuthorRegistryInfo(
        @Size(max = 255, message = "Не более 255 символов")
        @Schema(description = "ФИО",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                example = "Иванов Иван Иванович",
                nullable = true)
        String fullName,
        
        @Size(max = 255, message = "Не более 255 символов")
        @Schema(description = "Табельный номер",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                example = "123456789",
                nullable = true)
        String personnelNumber
) {
}
