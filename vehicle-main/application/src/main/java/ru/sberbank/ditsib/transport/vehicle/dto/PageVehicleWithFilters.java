package ru.sberbank.ditsib.transport.vehicle.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import ru.sberbank.ditsib.transport.vehicle.dto.vehicle.VehicleSearchFilters;
import ru.sberbank.ditsib.transport.vehicle.dto.vehicle.VehicleShortDto;

import java.util.List;

@Schema(description = "Струтура ответа при поиске автомобилей")
@Builder
public record PageVehicleWithFilters(
        @Schema(description = "Список найденных автомобилей", requiredMode = Schema.RequiredMode.REQUIRED)
        List<VehicleShortDto> content,
        @Schema(description = "Общее количество найденных элементов", requiredMode = Schema.RequiredMode.REQUIRED)
        long totalElements,
        @Schema(description = "Всего страниц с заданными критериями размера страницы", requiredMode = Schema.RequiredMode.REQUIRED)
        int totalPages,
        @Schema(description = "Размер полуения данных на одной странице", requiredMode = Schema.RequiredMode.REQUIRED)
        int size,
        @Schema(description = "Номер страницы", requiredMode = Schema.RequiredMode.REQUIRED)
        int number,
        @Schema(description = "Актуальное найденное колличество элементов на странице", requiredMode = Schema.RequiredMode.REQUIRED)
        int numberOfElements,
        @Schema(description = "Фитры, выбранны по искомым параметрам", requiredMode = Schema.RequiredMode.REQUIRED)
        VehicleSearchFilters filters
) {
}
