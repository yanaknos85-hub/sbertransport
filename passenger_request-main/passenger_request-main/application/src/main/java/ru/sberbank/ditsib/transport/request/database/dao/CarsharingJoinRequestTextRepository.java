package ru.sberbank.ditsib.transport.request.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sberbank.ditsib.transport.request.database.model.carsharing.CarsharingJoinRequestText;

import java.util.Optional;
import java.util.UUID;

public interface CarsharingJoinRequestTextRepository extends JpaRepository<CarsharingJoinRequestText, UUID> {
    
    Optional<CarsharingJoinRequestText> findByOrganizationId(UUID organizationId);
    
    void deleteByOrganizationId(UUID organizationId);
}
