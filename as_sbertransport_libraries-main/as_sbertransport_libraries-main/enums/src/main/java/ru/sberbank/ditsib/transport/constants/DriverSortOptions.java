package ru.sberbank.ditsib.transport.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Типы сортировки водителей.
 */
@Getter
@RequiredArgsConstructor
public enum DriverSortOptions {

    /**
     * Статус.
     */
    DRIVER_STATUS("Статус водителя"),

    /**
     * Автопарк.
     */
    AUTOPARK_NAME("Название автопарка"),

    /**
     * ФИО.
     */
    DRIVER_FULL_NAME("ФИО водителя"),

    /**
     * Рейтинг.
     */
    DRIVER_RATING("Рейтинг водителя");

    private final String description;
}
