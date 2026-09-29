package ru.sber.transport.request.external.model.waypoint;

import java.math.BigDecimal;
import java.util.UUID;

/**
 *
 * @param id  Идентификатор маршрутной точки
 * @param country Название страны
 * @param region Название региона
 * @param city Название города
 * @param street Название улицы
 * @param house Номер дома
 * @param building Номер корпуса
 * @param structure Номер строения
 * @param latitude Широта
 * @param longitude Долгота
 */
public record WaypointDTO(
        UUID id,
        String country,
        String region,
        String city,
        String street,
        String house,
        String building,
        String structure,
        BigDecimal latitude,
        BigDecimal longitude
) {}
