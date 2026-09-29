package ru.sber.transport.request.external.model;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Маршрутная точка
 */
public interface WaypointData {

    /**
     * Идентификатор маршрутной точки
     *
     * @return идентификатор маршрутной точки
     */
    UUID getId();

    /**
     * Название страны
     *
     * @return название страны
     */
    String getCountry();

    /**
     * Название региона
     *
     * @return название региона
     */
    String getRegion();

    /**
     * Название города
     *
     * @return название города
     */
    String getCity();

    /**
     * Название улицы
     *
     * @return название улицы
     */
    String getStreet();

    /**
     * Номер дома
     *
     * @return номер дома
     */
    String getHouse();

    /**
     * Номер корпуса
     *
     * @return номер корпуса
     */
    String getBuilding();

    /**
     * Номер строения
     *
     * @return номер строения
     */
    String getStructure();

    /**
     * Широта
     *
     * @return широта
     */
    BigDecimal getLatitude();

    /**
     * Долгота
     *
     * @return долгота
     */
    BigDecimal getLongitude();

}
