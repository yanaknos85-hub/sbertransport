package ru.sberbank.ditsib.geo_zones.use_cases.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

/**
 * Бизнес-объект геозон.
 */
@Setter
@Getter
@ToString
public class GeoZone {
    
    /**
     * Идентификатор.
     */
    private UUID id;
    
    /**
     * Идентификатор родителя.
     */
    private UUID parentId;
    
    /**
     * Код геозоны.
     */
    private String code;
    
    /**
     * Название геозоны.
     */
    private String name;

    /**
     * Временная зона.
     */
    private String timeZone;
}
