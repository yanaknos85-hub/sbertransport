package ru.sberbank.ditsib.geo_zones.providers.geo_zone.dao;

import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.sberbank.ditsib.geo_zones.providers.geo_zone.model.GeoZone;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий для работы с геозонами.
 */
public interface GeoZoneRepository extends JpaRepository<GeoZone, UUID>, SearchRepository {
    
    /**
     * Получение верхнеуровневых зон с именем.
     *
     * @param name имя зоны.
     *
     * @return зона.
     */
    Optional<GeoZone> findByNameAndParentIsNull(@NonNull String name);
    
    /**
     * Получение верхнеуровневых зон с кодом.
     *
     * @param code код зоны.
     *
     * @return зона.
     */
    Optional<GeoZone> findByCodeAndParentIsNull(@NonNull String code);
    
    /**
     * Получение зон на одном уровне с выбранной с кодом.
     *
     * @param id идентификатор зоны для поска дочерней.
     * @param code код зоны.
     *
     * @return зона.
     */
    Optional<GeoZone> findByCodeAndIdIsNot(@NonNull String code, @NonNull UUID id);
    
    /**
     * Получение зон на одном уровне с выбранной с именем.
     *
     * @param id идентификатор зоны для поска дочерней.
     * @param name имя зоны.
     *
     * @return зона.
     */
    Optional<GeoZone> findByNameAndIdIsNot(@NonNull String name, @NonNull UUID id);
    
    
    /**
     * Получение списка дочерних зон
     *
     * @param parentId идентификатор родительской зоны.
     *
     * @return список зон
     */
    List<GeoZone> findByParentId(UUID parentId);
    
    /**
     * Получение геозоны по коду.
     *
     * @param code код зоны.
     * @return геозона.
     */
    Optional<GeoZone> findByCode(@NonNull String code);
}
