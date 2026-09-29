package ru.sberbank.ditsib.transport.request.database.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Типы сообщений о фроде
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum FraudType {

    /**
     * Сообщение о фроде по чеку
     */
    RECEIPT("Чек"),

    /**
     * Сообщение о фроде по радиусу
     */
    RADIUS("Радиус"),

    /**
     * Сообщение о фроде на дробление поездки
     */
    SPLIT("Дробление поездки"),

    ABSENCE("Отсутствие на рабочем месте"),

    /**
     * Сообщение о фроде по превышению суммарного километража
     */
    OVERRUN("Превышение пробега в месяц"),

    /**
     * Сообщение о фроде по превышению суммарно допустимой продолжительности поездок в сутки
     */
    DURATION("Превышен лимит длительности поездок за сутки"),

    /**
     * Сообщение о фроде по превышению длительности одной заявки
     */
    SINGLE_TRIP_DURATION("Превышен лимит длительности одной заявки"),

    /**
     * Сообщение о фроде по превышению времени ожидания такси
     */
    TAXI_WAITING_TIME("Время ожидания такси превышает {limit} минут и противоречит условиям предоставления такси");

    private final String description;

}
