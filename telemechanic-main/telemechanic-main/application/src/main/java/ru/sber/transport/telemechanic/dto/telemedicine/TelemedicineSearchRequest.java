package ru.sber.transport.telemechanic.dto.telemedicine;

import io.swagger.v3.oas.annotations.media.Schema;
import ru.sber.transport.telemechanic.dto.DateRange;
import ru.sber.transport.telemechanic.dto.PageSettingDto;
import ru.sber.transport.telemechanic.enumerate.TelemedicineStatus;

import java.util.Set;
import java.util.UUID;

@Schema(title = "Запрос на поиск телемедицины", description = "Запрос на поиск телемедицины")
public record TelemedicineSearchRequest(
        @Schema(description = "Значение, введенное пользователем")
        String searchText,
        @Schema(description = "Идентификатор организации")
        UUID organizationId,
        @Schema(description = "Перечень статусов")
        Set<TelemedicineStatus> requestStatusSet,
        @Schema(description = "Период даты и времени создания заявки")
        DateRange creationTime,
        @Schema(description = "Период даты и времени принятиях решения")
        DateRange medicDecision,
        @Schema(description = "Параметры пагинации")
        PageSettingDto pageSetting
) {
}
