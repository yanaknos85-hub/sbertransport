package ru.sber.transport.cargo.exchange.request.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Sort;
import ru.sber.transport.cargo.exchange.request.database.model.Request_;
import ru.sber.transport.cargo.exchange.request.dto.common.FilterComponents;
import ru.sber.transport.cargo.exchange.request.enums.RequestStatus;

import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Schema(description = "DTO для поиска доставок грузовладельца")
public class ShipperRequestFilterDto {

    @Schema(description="Человекочитаемый идентификатор")
    private String humanreadableId;

    @Schema(description="Организация пользователя")
    private UUID organizationId;

    @Schema(description = "Статус доставки")
    @Enumerated(EnumType.STRING)
    private Set<RequestStatus> statusSet;

    @Schema(description = "Дата создания, диапазон")
    private FilterComponents.DateRange creationDateRange;

    @Schema(description = "Дата погрузки, диапазон")
    private FilterComponents.DateRange loadingDateRange;

    @Schema(description = "Дата доставки, диапазон")
    private FilterComponents.DateRange deliveryDateRange;

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