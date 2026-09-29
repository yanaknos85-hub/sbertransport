package ru.sberbank.ditsib.transport.request.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sberbank.ditsib.transport.request.database.model.Waypoint;

import java.util.UUID;

/**
 * Репозиторий точек маршрута
 */
@Repository
public interface WaypointRepository extends JpaRepository<Waypoint, UUID> {
}
