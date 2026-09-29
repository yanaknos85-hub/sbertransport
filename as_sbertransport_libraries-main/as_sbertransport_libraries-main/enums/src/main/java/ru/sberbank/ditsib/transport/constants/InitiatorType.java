package ru.sberbank.ditsib.transport.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Тип инициатора.
 */
@Getter
@RequiredArgsConstructor
public enum InitiatorType {

    /**
     * Сотрудник.
     */
    EMPLOYEE("Сотрудник"),

    /**
     * Диспетчер.
     */
    DISPATCHER("Диспетчер");

    private final String description;
}
