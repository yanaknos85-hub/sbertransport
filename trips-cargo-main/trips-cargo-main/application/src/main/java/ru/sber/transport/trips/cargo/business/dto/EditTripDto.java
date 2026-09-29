package ru.sber.transport.trips.cargo.business.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import ru.sber.transport.trips.cargo.business.model.TripStatus;

import java.util.UUID;

@Schema(title = "Поездка",
        description = "Данные поездки, доступные для редактирования")
public record EditTripDto(
        TripStatus status,
        UUID driverId,
        Double factDistance,
        Long driverWaitingTime
) implements EditTripDataDto {
}
