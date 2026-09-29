package ru.sberbank.ditsib.transport.vehicle.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sberbank.ditsib.transport.vehicle.database.model.EngineType;

import java.util.Optional;
import java.util.UUID;

public interface EngineTypeRepository extends JpaRepository<EngineType, UUID> {
    Optional<EngineType> findByTitle(String title);
}
