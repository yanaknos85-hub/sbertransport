package ru.sber.transport.notifications.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.sber.transport.notifications.database.dao.messages.trip_request.TripPurposeRepository;
import ru.sber.transport.notifications.database.model.TripPurpose;
import ru.sber.transport.notifications.services.TripPurposeService;

import java.util.UUID;

@Component
@RequiredArgsConstructor
class TripPurposeServiceImpl implements TripPurposeService {
    
    private final TripPurposeRepository tripPurposeRepository;
    
    @Override
    public TripPurpose get(UUID purposeId) {
        return tripPurposeRepository.findById(purposeId).orElseGet(() -> save(new TripPurpose(purposeId)));
    }
    
    @Override
    public TripPurpose save(TripPurpose purpose) {
        return tripPurposeRepository.save(purpose);
    }
}
