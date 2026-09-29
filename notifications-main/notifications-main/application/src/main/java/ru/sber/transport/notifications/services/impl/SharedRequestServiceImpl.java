package ru.sber.transport.notifications.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sber.transport.notifications.database.dao.messages.trip_request.TripSharedRideRepository;
import ru.sber.transport.notifications.database.model.request.SharedRide;
import ru.sber.transport.notifications.services.SharedRequestService;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SharedRequestServiceImpl implements SharedRequestService {
    
    private final TripSharedRideRepository sharedRequestRepository;
    
    @Override
    public Optional<SharedRide> get(UUID id) {
        return sharedRequestRepository.findById(id);
    }
    
    @Override
    public SharedRide save(SharedRide sharedRequest) {
        return sharedRequestRepository.save(sharedRequest);
    }
}
