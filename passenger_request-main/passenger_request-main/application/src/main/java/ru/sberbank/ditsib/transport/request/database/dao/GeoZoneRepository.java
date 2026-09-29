package ru.sberbank.ditsib.transport.request.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sberbank.ditsib.transport.request.database.model.messages.GeoZone;

import java.util.UUID;

/**
 * Репозиторий для работы с геозонами.
 */
public interface GeoZoneRepository extends JpaRepository<GeoZone, UUID> {
}
