package ru.sber.transport.cargo.exchange.request.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import ru.sber.transport.cargo.exchange.request.dto.common.FilterComponents;

import java.util.Set;

@Getter
@Setter
@Schema(description = "DTO для поиска доступных доставок")
public class MarketplaceRequestFilterDto {

    @Schema(description = "Город отправления", example = "Москва")
    private String fromCity;

    @Schema(description = "Город назначения", example = "Санкт-Петербург")
    private String toCity;

    @Schema(description = "Тип кузова", example = "рефрижератор")
    private Set<String> bodyTypes;

    @Schema(description = "Дата погрузки, диапазон")
    private FilterComponents.DateRange loadingDateRange;

    @Schema(description = "Вес груза в тоннах, диапазон", example = "5.0")
    private FilterComponents.BigDecimalRange weightRange;

    @Schema(description = "Настройки разделения на страницы")
    private FilterComponents.PageSetting pageSetting;
}