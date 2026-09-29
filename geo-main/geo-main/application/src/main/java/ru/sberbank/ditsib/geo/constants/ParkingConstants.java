package ru.sberbank.ditsib.geo.constants;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * Available types of parking.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public enum ParkingConstants {

    /**
     * Moscow parking.
     */
    MOSCOW_PARKING("Московский паркинг");

    private final String description;

    /**
     * Check equality of name with any parking type.
     *
     * @param name name of packing.
     * @return <code>true</code> if the name is equals with name of one of parking types.
     */
    public static boolean matchingCheck(String name){
        return Arrays.stream(ParkingConstants.values()).map(ParkingConstants::getDescription).anyMatch(name::startsWith);
    }
}
