package ru.sber.transport.cargo.exchange.request.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Sort;
import ru.sber.transport.cargo.exchange.request.database.model.Request_;
import ru.sber.transport.cargo.exchange.request.dto.common.FilterComponents;

import java.math.BigDecimal;
import java.util.List;

@Builder
@Getter
@Setter
@Schema(description = "Фильтр для получения списка заявок грузоперевозчика в разделе 'Мои заявки'")
public class CarrierRequestFilterDto {

    @Schema(description = "Человеко-читаемый номер заявки: формат ОП-ГГГГММ-ННННННН, например ОП-202601-0000001", example = "ОП-202601-0000001")
    private String humanReadableId;

    @Schema(description = "Адрес погрузки", example = "г. Москва, ул. Ленина, 10")
    private String addressFrom;

    @Schema(description = "Адрес выгрузки", example = "г. Санкт-Петербург, Невский пр., 50")
    private String addressTo;

    @Schema(description = "Список статусов заявки", example = "[\"PUBLISHED\", \"ASSIGNED\"]")
    private List<String> statusSet;

    @Schema(description = "Дата погрузки, диапазон")
    private FilterComponents.DateRange loadingDateRange;

    @Schema(description = "Дата выгрузки, диапазон")
    private FilterComponents.DateRange deliveryDateRange;

    @Schema(description = "Ставка за перевозку", example = "750000.00")
    private BigDecimal rate;

    @Schema(description = "Флаг использования ЕТРН", example = "true")
    private Boolean etrn;

    @Schema(description = "Настройки сортировки")
    private FilterComponents.SortSetting sortSetting;

    @Schema(description = "Настройки разделения на страницы")
    private FilterComponents.PageSetting pageSetting;

    public Sort getSort() {
        if (sortSetting == null){
            return Sort.by(Sort.Direction.DESC, Request_.CREATED_AT);
        }
        return this.sortSetting.getSort();
    }
}