package ru.sber.transport.dispatcher.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.sber.transport.dispatcher.database.dao.TripsRepository;
import ru.sber.transport.dispatcher.database.model.Trip;
import ru.sber.transport.dispatcher.service.TripsService;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TripsServiceImpl implements TripsService {

    private final TripsRepository tripsRepository;

    @Override
    public Trip save(Trip trip) {
        return tripsRepository.save(trip);
    }

    @Override
    public void delete(UUID tripId) {
        tripsRepository.deleteById(tripId);
    }
}
