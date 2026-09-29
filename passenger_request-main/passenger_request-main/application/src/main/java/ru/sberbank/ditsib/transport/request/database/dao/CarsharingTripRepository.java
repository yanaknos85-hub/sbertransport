package ru.sberbank.ditsib.transport.request.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sberbank.ditsib.transport.request.database.model.carsharing.CarsharingTrip;

import java.util.Optional;
import java.util.UUID;

public interface CarsharingTripRepository extends JpaRepository<CarsharingTrip, UUID> {
    
    Optional<CarsharingTrip> findFirstByRentId(Integer rentId);
    
    Optional<CarsharingTrip> findFirstByRequestId(UUID requestId);
    
}
