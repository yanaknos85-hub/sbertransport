package ru.sberbank.ditsib.transport.tariff.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sberbank.ditsib.transport.tariff.database.model.messages.GeoZone;

import java.util.*;

/**
 * Репозиторий для работы с геозонами.
 */
public interface GeoZoneRepository extends JpaRepository<GeoZone, UUID> {
    
    /**
     * Поиск геозоны по имени.
     *
     * @param geoZone имя геозоны.
     * @return геозона.
     */
    Optional<GeoZone> findByName(String geoZone);
}
