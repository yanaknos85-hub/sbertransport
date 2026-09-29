package ru.sber.transport.trips.cargo.business.dto;

import ru.sber.transport.trips.cargo.business.model.TripStatus;

import java.util.UUID;

public interface EditTripDataDto {
    TripStatus status();
    UUID driverId();
    Double factDistance();
}
