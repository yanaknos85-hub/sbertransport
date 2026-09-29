package ru.sberbank.ditsib.transport.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Типы личного транспорта.
 */
@Getter
@RequiredArgsConstructor
public enum  PersonalTransportType {

    /**
     * Авто.
     */
    CAR("Автомобиль"),

    /**
     * Мото.
     */
    MOTORCYCLE("Мотоцикл");

    private final String rusName;
}
