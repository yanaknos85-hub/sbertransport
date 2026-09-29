package ru.sber.transport.request.external.model;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Duration;

/**
 * Цена на услугу
 */
public interface PriceData {

    /**
     * Стоимость услуги
     *
     * @return стоимость услуги
     */
    BigDecimal price();

    /**
     * Длительность услуги
     *
     * @return длительность услуги
     */
    Duration duration();

    /**
     * Ссылка на создание заказа
     *
     * @return ссылка на создание заказа
     */
    URI link();

    /**
     * Расстояние в метрах
     * @return расстояние в метрах
     */
    long distance();

}
