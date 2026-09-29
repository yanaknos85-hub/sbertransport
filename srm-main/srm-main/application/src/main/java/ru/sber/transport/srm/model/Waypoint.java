package ru.sber.transport.srm.model;

import java.util.UUID;

/**
 * Интерфейс путевой точки.
 */
public interface Waypoint {
    
    /**
     * Получить идентификатор точки.
     *
     * @return идентификатор точки.
     */
    UUID getId();
    
    /**
     * Получить широту точки.
     *
     * @return широта точки.
     */
    Double getLatitude();
    
    /**
     * Получить долготу точки.
     *
     * @return долгота точки.
     */
    Double getLongitude();
}
