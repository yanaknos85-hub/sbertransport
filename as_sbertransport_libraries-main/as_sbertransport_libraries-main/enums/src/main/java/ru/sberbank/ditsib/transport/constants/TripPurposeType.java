package ru.sberbank.ditsib.transport.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.NoSuchElementException;

/**
 * Тип цели поездки
 */
@Getter
@RequiredArgsConstructor
public enum TripPurposeType {

    /**
     * Личная поездка.
     */
    PERSONAL("Личная"),

    /**
     * Корпоративная поездка.
     */
    CORPORATE("Корпоративная");

    /**
     * Получение типа поездки по русскому названию.
     *
     * @param rusName русское название.
     *
     * @return тип поездки.
     */
    public static TripPurposeType valueOfRus(String rusName) {
        for (var type : TripPurposeType.values()) {
            if (type.getRusName().equalsIgnoreCase(rusName)) {
                return type;
            }
        }
        throw new NoSuchElementException(String.format("No TripPurposeType found for '%s'", rusName));
    }
    
    private final String rusName;
}
