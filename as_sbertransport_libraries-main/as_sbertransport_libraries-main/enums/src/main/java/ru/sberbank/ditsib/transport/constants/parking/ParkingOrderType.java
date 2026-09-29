package ru.sberbank.ditsib.transport.constants.parking;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Тип бронирования парковочного места
 */
@Getter
@RequiredArgsConstructor
public enum ParkingOrderType {

    /**
     * Короткое бронирование (до 1 дня).
     */
    SHORT("short", "Бронирование  пользователем из общего пула на 1 день"),

    /**
     * Долгое бронирование.
     */
    LONG("long", "Бронирование координатором на продолжительный срок");
    
    @JsonValue
    private final String jsonValue;

    private final String description;
}
