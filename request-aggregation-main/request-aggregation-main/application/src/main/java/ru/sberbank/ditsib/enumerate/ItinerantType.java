package ru.sberbank.ditsib.enumerate;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Тип характера работы
 */
@Getter
@AllArgsConstructor
public enum ItinerantType {

    NONE("Не разъездной"),
    FULL("Разъездной"),
    PARTIAL("Частично разъездной");

    private final String description;
}
