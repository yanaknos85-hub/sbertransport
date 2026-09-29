package ru.sberbank.ditsib.enumerate;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Optional;

@Getter
@AllArgsConstructor
public enum TransportClass {

    ECONOMY("Эконом"),
    COMFORT("Комфорт"),
    COMFORT_PLUS("Комфорт+"),
    BUSINESS("Бизнес"),
    NONE("Служебное авто");

    private final String description;

    public static Optional<TransportClass> getTransportClass(String transportClass) {
        if (transportClass != null && !transportClass.isBlank()) {
            for (var type : TransportClass.values()) {
                if (transportClass.equalsIgnoreCase(type.name())) {
                    return Optional.of(type);
                }
            }
        }
        return Optional.empty();
    }
}