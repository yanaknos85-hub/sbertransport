package ru.sberbank.ditsib.transport.request.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sberbank.ditsib.transport.request.database.model.carsharing.CarsharingExternalData;

import java.util.Optional;
import java.util.UUID;

public interface CarsharingExternalDataRepository extends JpaRepository<CarsharingExternalData, UUID> {
    
    Optional<CarsharingExternalData> findFirstByOrganizationId(UUID organizationId);
    
}
