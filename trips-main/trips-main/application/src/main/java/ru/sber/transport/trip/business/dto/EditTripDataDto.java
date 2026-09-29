package ru.sber.transport.trip.business.dto;

import ru.sber.transport.trip.business.model.TripStatus;

import java.util.UUID;

public interface EditTripDataDto {
    TripStatus status();
    UUID driverId();
    Double factDistance();
}
