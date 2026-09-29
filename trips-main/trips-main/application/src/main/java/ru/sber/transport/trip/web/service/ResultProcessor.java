package ru.sber.transport.trip.web.service;

import ru.sber.transport.trip.business.dto.GetTripResponse;
import ru.sber.transport.trip.business.dto.TripV2Dto;
import ru.sber.transport.trip.business.model.Trip;

public interface ResultProcessor {

    Iterable<TripV2Dto> process (Iterable<Trip> result);

    TripV2Dto process (Trip result);

    GetTripResponse processForIntegration (Trip result);

}
