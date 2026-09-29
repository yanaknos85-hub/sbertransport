package ru.sber.transport.telemechanic.dto.medic;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import ru.sber.transport.telemechanic.dto.PageSettingDto;

@Schema(title = "Поиск медицинской лицензии", description = "Запрос на поиск по фамилии медика")
public record MedicalLicenseSearchDto(
        
        @Schema(description = "Параметры пагинации")
        PageSettingDto page,
        
        @Size(max = 20)
        @Schema(description = "Фамилия медика", maxLength = 20)
        String searchText
) {
}
