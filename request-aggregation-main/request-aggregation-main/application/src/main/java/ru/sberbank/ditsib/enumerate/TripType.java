package ru.sberbank.ditsib.enumerate;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Optional;

@Getter
@AllArgsConstructor
public enum TripType {

    DAYTIME_TRIP("Поездка в дневное время"),
    HIGHTIME_EMPLOYEE_TRANSPORTATION("Доставка работников в ночное время");

    private final String description;

    public static Optional<TripType> getTripType(String tripType) {
        if (tripType != null && !tripType.isBlank()) {
            for (var type : TripType.values()) {
                if (tripType.equalsIgnoreCase(type.name())) {
                    return Optional.of(type);
                }
            }
        }
        return Optional.empty();
    }
}
