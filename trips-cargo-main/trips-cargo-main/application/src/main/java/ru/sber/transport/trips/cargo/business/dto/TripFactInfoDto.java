package ru.sber.transport.trips.cargo.business.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import ru.sber.transport.trips.cargo.business.model.TripStatus;

@Schema(title = "Поездка", description = "Данные по завершенной поездке")
public record TripFactInfoDto(
        TripStatus status,
        Long startTime,
        Long endTime,
        Double passedDistance
) {
}
