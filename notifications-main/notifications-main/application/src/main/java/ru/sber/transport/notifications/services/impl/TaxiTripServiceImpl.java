package ru.sber.transport.notifications.services.impl;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sber.transport.notifications.database.dao.messages.trip_request.TaxiTripRepository;
import ru.sber.transport.notifications.database.model.request.TaxiTrip;
import ru.sber.transport.notifications.services.TaxiTripService;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaxiTripServiceImpl implements TaxiTripService {
    
    private final TaxiTripRepository taxiTripRepository;
    
    @Override
    public TaxiTrip getByRequestId(UUID requestId) {
        return taxiTripRepository.findByRequestId(requestId);
    }
    
    @Override
    public TaxiTrip getBySharedRideId(UUID magentaId) {
        return taxiTripRepository.findBySharedRideId(magentaId);
    }
    
    @Override
    public TaxiTrip save(TaxiTrip taxiTrip) {
        if (taxiTrip.getId() == null) {
            log.warn("Trip without ID: {}", taxiTrip);
            return null;
        }
        return taxiTripRepository.save(taxiTrip);
    }

    @Override
    public Optional<TaxiTrip> get(@NonNull UUID id) {
        return taxiTripRepository.findById(id);
    }
}
