package ru.sberbank.ditsib.transport.reports.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sberbank.ditsib.transport.reports.dao.SharedRideRepository;
import ru.sberbank.ditsib.transport.reports.model.magenta.SharedRide;
import ru.sberbank.ditsib.transport.reports.service.MagentaSharedRequestService;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class MagentaSharedRequestServiceImpl implements MagentaSharedRequestService {
    private final SharedRideRepository sharedRideRepository;
    
    @Override
    public Optional<SharedRide> findById(UUID sharedRideId) {
        return sharedRideRepository.findById(sharedRideId);
    }
    
    @Override
    public SharedRide save(SharedRide sharedRequest) {
        return sharedRideRepository.save(sharedRequest);
    }
}
