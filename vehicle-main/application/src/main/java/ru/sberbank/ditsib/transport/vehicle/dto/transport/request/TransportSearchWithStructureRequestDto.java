package ru.sberbank.ditsib.transport.vehicle.dto.transport.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import ru.sberbank.ditsib.transport.vehicle.dto.PageSettingDto;

@Schema(description = "DTO для запроса поиска транспортного средства сотрудника, с добавлением транспортных средств по его штатной структуре")
public record TransportSearchWithStructureRequestDto(
        @Schema(description = "Параметры пагинации", requiredMode = Schema.RequiredMode.REQUIRED)
        PageSettingDto page,
        @Schema(description = "Государственный номер или его часть", minLength = 3, maxLength = 9, requiredMode = Schema.RequiredMode.REQUIRED,
                example = "A12")
        @Size(min = 3, max = 9)
        String searchText
) {
}