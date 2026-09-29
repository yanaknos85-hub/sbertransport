package ru.sber.transport.request.external.model;

import java.math.BigDecimal;
import java.time.Duration;

/**
 * Интерфейс информации о заявке на поездку
 */
public interface OrderData {

    /**
     * Стоимость поездки
     *
     * @return стоимость поездки
     */
    BigDecimal getCost();

    /**
     * Время поездки
     *
     * @return время поездки
     */
    Duration getDuration();

    /**
     * Расстояние поездки
     *
     * @return расстояние поездки
     */
    long getDistance();

}
