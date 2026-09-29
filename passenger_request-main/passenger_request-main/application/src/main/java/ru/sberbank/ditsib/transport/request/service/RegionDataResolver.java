package ru.sberbank.ditsib.transport.request.service;

import ru.sberbank.ditsib.transport.request.dto.RegionDto;
import ru.sberbank.ditsib.transport.request.dto.WaypointDTO;

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
