package ru.sberbank.ditsib.transport.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * типы сортировок заявок
 */
@Getter
@RequiredArgsConstructor
public enum RequestSortOption {

    /**
     * ФИО.
     */
    PASSENGER_FULL_NAME("ФИО пассажира"),

    /**
     * Дата поездки.
     */
    DESIRED_DATE("Дата поездки"),

    /**
     * Время создания.
     */
    CREATION_DATE("Время создания поездки"),

    /**
     * Стоимость.
     */
    EXPECTED_COST("Стоимость"),

    /**
     * Идентификатор.
     */
    REQUEST_ID("ID поездки"),

    /**
     * Человекочитаемый идентификатор.
     */
    REQUEST_HUMAN_ID("ID поездки человекочитаемый"),

    /**
     * Идентификатор лимита.
     */
    LIMIT_ID("ID лимита");

    private final String description;
}
