package ru.sber.transport.telemechanic.enumerate;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Типы сортировок
 */
@Getter
@RequiredArgsConstructor
public enum RequestSortOption {

    ID("Идентификатор записи"),
    HUMAN_READABLE_ID("Человекочитаемый идентификатор"),
    ORGANIZATION_OFFICIAL_NAME("Организация, служебное название"),
    FULL_NAME("ФИО"),
    CREATION_DATE("Дата и время создания"),
    WORK_ORDER_TOTAL_PRICE("Заказ-наряд, итоговая цена");
    private final String description;
}
