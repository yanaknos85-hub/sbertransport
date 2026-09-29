package ru.sber.transport.notifications.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.sber.transport.notifications.database.dao.messages.trip_request.TripRequestRepository;
import ru.sber.transport.notifications.database.model.request.TripRequest;
import ru.sber.transport.notifications.services.TripRequestService;

import jakarta.transaction.Transactional;
import java.util.Set;
import java.util.UUID;

/**
 * Сервис по работе с заявками на поездку.
 */
@Component
@Transactional
@RequiredArgsConstructor
class TripRequestServiceImpl implements TripRequestService {
    
    private final TripRequestRepository tripRequestRepository;
    
    @Override
    public TripRequest get(UUID id) {
        return tripRequestRepository.findById(id).orElseThrow();
    }
    
    @Override
    public void delete(UUID requestId) {
        tripRequestRepository.deleteById(requestId);
    }
    
    @Override
    public TripRequest save(TripRequest request) {
        return tripRequestRepository.save(request);
    }
    
    @Override
    public Set<TripRequest> getAllBySharedRideId(UUID id) {
        return tripRequestRepository.findAllBySharedRideId(id);
    }
    
    @Override
    public TripRequest saveAndFlush(TripRequest request) {
        return tripRequestRepository.saveAndFlush(request);
    }
    
}
