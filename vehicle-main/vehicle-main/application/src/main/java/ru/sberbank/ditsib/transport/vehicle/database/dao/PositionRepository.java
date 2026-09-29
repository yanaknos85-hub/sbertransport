package ru.sberbank.ditsib.transport.vehicle.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sberbank.ditsib.transport.vehicle.database.model.Position;

import java.util.UUID;

/**
 * Repository of positions
 */
public interface PositionRepository extends JpaRepository<Position, UUID> {
}
