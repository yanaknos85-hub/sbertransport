package ru.sberbank.ditsib.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sberbank.ditsib.database.model.Position;

import java.util.UUID;

/**
 * Repository of positions
 */
@Repository
public interface PositionRepository extends JpaRepository<Position, UUID> {
}
