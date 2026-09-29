package ru.sberbank.ditsib.geo_zones.use_cases;

import lombok.NonNull;
import ru.sberbank.ditsib.geo_zones.use_cases.model.GeoZone;
import ru.sberbank.ditsib.geo_zones.use_cases.model.GeoZoneWithChildren;
import ru.sberbank.ditsib.geo_zones.web.http.dto.WaypointDto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Сервис по работе с геозонами.
 */
public interface GeoZoneCases {
    
    /**
     * Сохранение.
     *
     * @param data данные.
     *
     * @return сохраненные данные.
     */
    GeoZone save(@NonNull GeoZone data);
    
    /**
     * Сохранение.
     *
     * @param id идентификатор. <code>null</code> если добавляем.
     * @param data данные.
     *
     * @return сохраненные данные.
     */
    GeoZone save(UUID id, @NonNull GeoZone data);
    
    /**
     * Удаление.
     *
     * @param id идентификатор.
     */
    void delete(@NonNull UUID id);
    
    /**
     * Получить.
     *
     * @param id идентификатор.
     *
     * @return геозона.
     */
    GeoZone get(@NonNull UUID id);
    
    /**
     * Получить все.
     *
     * @return список зон.
     */
    List<GeoZone> getAll();
    
    /**
     * Получить геозоны корневого уровня.
     *
     * @return список зон.
     */
    List<GeoZoneWithChildren> getRoots();
    
    
    /**
     * Получить список дочерних зон.
     *
     * @return список зон.
     */
    List<GeoZone> getChildren(@NonNull UUID parentId);
    
    /**
     * Поиск геозоны по данным адреса.
     *
     * @param waypoint данные адреса.
     *
     * @return геозоны.
     */
    GeoZone search(WaypointDto waypoint);
    
    /**
     * Поиск ветки геозон по данным адреса
     *
     * @param waypoint данные адреса.
     *
     * @return список геозон.
     */
    List<GeoZone> searchBranch(WaypointDto waypoint);
    
    /**
     * Поиск геозоны по коду.
     *
     * @param code код для поиска.
     *
     * @return геозона.
     */
    Optional<GeoZone> find(String code);
}
