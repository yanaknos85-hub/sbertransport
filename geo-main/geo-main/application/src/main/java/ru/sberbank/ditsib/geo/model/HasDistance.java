package ru.sberbank.ditsib.geo.model;

import java.util.Optional;

/**
 * Объект расстояния.
 */
public interface HasDistance {
    
    /**
     * Получение расстояния.
     *
     * @return расстояние.
     */
    Double getDistance();
    
    /**
     * Установка расстояния.
     *
     * @param distance новое значение.
     */
    void setDistance(Double distance);
    
    /**
     * Добавить расстояние.
     *
     * @param distance значение для добавления.
     */
    default void addDistance(double distance) {
        setDistance(Optional.ofNullable(getDistance()).orElse(0.0) + distance);
    }
    
}
