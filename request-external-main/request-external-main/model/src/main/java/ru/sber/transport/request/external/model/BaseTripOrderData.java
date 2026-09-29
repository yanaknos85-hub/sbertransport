package ru.sber.transport.request.external.model;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Заявка на поездку
 */
public interface BaseTripOrderData {

    /**
     * Дата поездки
     *
     * @return дата поездки
     */
    OffsetDateTime getDate();

    /**
     * Путевые точки поездки
     *
     * @return путевые точки поездки
     */
    List<WaypointData> getWaypoints();

    /**
     * Цель поездки
     *
     * @return цель поездки
     */
    UUID getPurposeId();

    /**
     * Тариф поездки
     *
     * @return тариф поездки
     */
    Tariff getTariff();

    /**
     * Комментарий к поездке
     *
     * @return комментарий к поездке
     */
    String getComment();

}
