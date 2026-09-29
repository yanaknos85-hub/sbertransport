package ru.sber.transport.constants;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enum Типы совмещения точек.
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum PointMatchingType {
    
    /**
     * Совмещение ПО ПУТИ всех точек маршрута.
     */
    MATCH_ALL_POINTS(false),
    
    /**
     * Совмещение ПО ПУТИ первой и последней точек маршрута.
     */
    MATCH_FIRST_AND_LAST_POINTS(true),
    
    /**
     * Точки маршрута не совмещаются.
     */
    MATCH_POINTS_FALSE(true);
    
    private final boolean optimized;
}
