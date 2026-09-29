package ru.sberbank.ditsib.geo.dto;

/**
 * Типы дорог, которые следует избегать при построении маршрута.
 */
public enum FilterEnum {
    DIRT_ROAD,
    TOLL_ROAD,
    FERRY,
    HIGHWAY,
    BAN_CAR_ROAD,
    BAN_STAIRWAY,
    BAN_OVER
}
