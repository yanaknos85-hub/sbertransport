package ru.sberbank.ditsib.geo_zones.use_cases.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

/**
 * Бизнес-объект геозон.
 */
@Setter
@Getter
public class GeoZoneWithChildren {
    
    /**
     * Идентификатор.
     */
    private UUID id;
    
    /**
     * Код родителя.
     */
    private String parentCode;
    
    
    /**
     * Название геозоны-родителя.
     */
    private String parentName;
    
    /**
     * Код геозоны.
     */
    private String code;
    
    /**
     * Название геозоны.
     */
    private String name;
    
    private List<GeoZoneWithChildren> children;
}
