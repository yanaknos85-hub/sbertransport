package ru.sber.transport.trips.cargo.web.service;

import ru.sber.transport.trips.cargo.business.dto.GetTripResponse;
import ru.sber.transport.trips.cargo.business.dto.TripV2Dto;
import ru.sber.transport.trips.cargo.business.model.Trip;

public interface ResultProcessor {

    Iterable<TripV2Dto> process (Iterable<Trip> result);

    TripV2Dto process (Trip result);

    GetTripResponse processForIntegration (Trip result);

}
