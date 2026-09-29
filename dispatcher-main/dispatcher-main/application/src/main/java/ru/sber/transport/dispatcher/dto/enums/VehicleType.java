package ru.sber.transport.dispatcher.dto.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * Тип перевозки, выполняемый транспортом
 */
@Getter
@RequiredArgsConstructor
public enum VehicleType {
    PASSENGER("Легковой"),
    CARGO("Грузовой"),
    UNIVERSAL("Универсальный");

    private final String category;

    public static VehicleType fromCategory(String category) {
        if (category == null) {
            return null;
        }

        return Arrays.stream(values())
                .filter(type -> type.getCategory().equalsIgnoreCase(category.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown vehicle category " + category));
    }
}
