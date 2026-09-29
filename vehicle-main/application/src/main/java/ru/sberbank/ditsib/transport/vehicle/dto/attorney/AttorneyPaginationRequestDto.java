package ru.sberbank.ditsib.transport.vehicle.dto.attorney;

import io.swagger.v3.oas.annotations.media.Schema;
import ru.sberbank.ditsib.transport.vehicle.dto.PageSettingDto;

@Schema(description = "Структура для получения списка доверенностей в пагинированном виде")
public record AttorneyPaginationRequestDto(
        PageSettingDto pageSetting
) {
}
