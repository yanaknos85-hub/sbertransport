package ru.sberbank.ditsib.transport.constants.parking;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Статус бронирования
 */
@Getter
@RequiredArgsConstructor
public enum ParkingOrderStatus {

    /**
     * Активный.
     */
    ACTIVE("Active","Активен"),

    /**
     * Отмененный.
     */
    CANCELLED("Cancelled","Отменён");
    
    @JsonValue
    private final String jsonValue;

    private final String description;
}
