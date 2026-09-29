package ru.sberbank.ditsib.transport.tariff.service;

import ru.sber.transport.tariff.model.WaypointDTO;
import ru.sberbank.ditsib.transport.tariff.dto.RegionDto;

import java.util.List;

/**
 * Region data resolver.
 */
public interface RegionDataResolver {
    
    /**
     * Resolve region data.
     *
     * @param waypoint data for resolving region.
     *
     * @return resolved region.
     */
    RegionDto getRegion(WaypointDTO waypoint);
    
    /**
     * Получить ветку геозон по данным
     *
     * @param waypoint данные для поиска
     *
     * @return список геозон, от самой глубокой по иерархии наверх
     */
    List<RegionDto> getRegionBranch(WaypointDTO waypoint);
}
