package ru.sberbank.ditsib.geo.service;

import java.util.Map;

/**
 * Service for working with regions.
 */
public interface RegionService {
    
    /**
     * Get region by ID.
     *
     * @param id ID of region.
     *
     * @return map of region.
     */
    Map<String, Object> get(String id);
    
}
