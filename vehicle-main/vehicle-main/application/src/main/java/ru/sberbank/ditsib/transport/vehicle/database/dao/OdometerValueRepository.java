package ru.sberbank.ditsib.transport.vehicle.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sberbank.ditsib.transport.vehicle.database.model.OdometerValue;

import java.util.List;
import java.util.UUID;

public interface OdometerValueRepository extends JpaRepository<OdometerValue, UUID> {
    
    List<OdometerValue> findByTransportId(UUID transportId);
}
