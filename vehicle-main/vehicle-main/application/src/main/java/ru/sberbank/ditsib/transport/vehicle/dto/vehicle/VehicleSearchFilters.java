package ru.sberbank.ditsib.transport.vehicle.dto.vehicle;

import io.swagger.v3.oas.annotations.media.Schema;
import ru.sberbank.ditsib.transport.vehicle.dto.FilterValue;

import java.math.BigDecimal;
import java.util.List;

public record VehicleSearchFilters(
        @Schema(description = "Данные справочника Тип кузова", requiredMode = Schema.RequiredMode.REQUIRED)
        List<FilterValue> bodyType,
        @Schema(description = "Данные справочника Тип двигателя", requiredMode = Schema.RequiredMode.REQUIRED)
        List<FilterValue> engineType,
        @Schema(description = "Данные справочника Тип трансмиссии", requiredMode = Schema.RequiredMode.REQUIRED)
        List<FilterValue> transmissionType,
        @Schema(description = "Данные справочника Тип привода", requiredMode = Schema.RequiredMode.REQUIRED)
        List<FilterValue> driveType,
        @Schema(description = "Доступные вариации значений Объема двигателя", requiredMode = Schema.RequiredMode.REQUIRED)
        List<Integer> engineCapacity,
        @Schema(description = "Доступные вариации значений Мощности дивгателя в лс", requiredMode = Schema.RequiredMode.REQUIRED)
        List<BigDecimal> enginePower,
        @Schema(description = "Доступные вариации значений Период производства", requiredMode = Schema.RequiredMode.REQUIRED)
        List<String> manufacturePeriod

) {
    private static VehicleSearchFilters EMPTY_FILTERS = new VehicleSearchFilters(List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of());
    public static VehicleSearchFilters emptyFilters() {
        return EMPTY_FILTERS;
    }
}