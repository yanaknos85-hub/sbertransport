package ru.sberbank.ditsib.transport.reports.service;

import ru.sberbank.ditsib.transport.reports.model.magenta.SharedRide;

import java.util.Optional;
import java.util.UUID;

public interface MagentaSharedRequestService {
    
    Optional<SharedRide> findById(UUID sharedRideId);
    
    SharedRide save(SharedRide build);
}
