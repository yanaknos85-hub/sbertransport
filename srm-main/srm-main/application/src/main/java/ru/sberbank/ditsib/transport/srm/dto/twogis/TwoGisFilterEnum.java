package ru.sberbank.ditsib.transport.srm.dto.twogis;

/**
 * Типы дорог, которые следует избегать при построении маршрута.
 */
public enum TwoGisFilterEnum {
    dirt_road,
    toll_road,
    ferry,
    highway,
    ban_car_road,
    ban_stairway,
    ban_over
}
