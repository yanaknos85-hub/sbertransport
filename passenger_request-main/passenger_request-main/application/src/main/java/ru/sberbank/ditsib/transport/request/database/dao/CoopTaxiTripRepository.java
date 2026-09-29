package ru.sberbank.ditsib.transport.request.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sberbank.ditsib.transport.request.database.model.CoopTaxiTrip;

import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий поездок на такси
 */
@Repository
public interface CoopTaxiTripRepository extends JpaRepository<CoopTaxiTrip, UUID> {
    
    Optional<CoopTaxiTrip> findByRideId(UUID rideId);
    
}
