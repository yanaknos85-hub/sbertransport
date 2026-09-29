package ru.sberbank.ditsib.transport.vehicle.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Запрос на поиск транспортных средств по гос.номеру")
public record StateNumberSearchRequestDto(
        
        @Size(min = 3, max = 9)
        @Schema(description = "Гос.номер авто", minLength = 3, maxLength = 9)
        String stateNumber,
        
        @Schema(description = "Параметры пагинации", requiredMode = Schema.RequiredMode.REQUIRED)
        PageSettingDto page
) {
}
