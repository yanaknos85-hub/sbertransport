package ru.sberbank.ditsib.enumerate;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Optional;
import java.util.Set;

@Getter
@AllArgsConstructor
public enum TransportType {

    TAXI(4, Set.of(TransportClass.COMFORT, TransportClass.ECONOMY, TransportClass.BUSINESS,
            TransportClass.COMFORT_PLUS, TransportClass.NONE)),
    PERSONAL(4, Set.of(TransportClass.NONE)),
    PUBLIC(null, Set.of(TransportClass.NONE)),
    CARSHARING(null, Set.of(TransportClass.NONE)),
    GROUP_TRANSFER(null, Set.of(TransportClass.NONE));

    private final Integer maxPassengers;
    private final Set<TransportClass> classes;

    public static Optional<TransportType> getTransportType(String transportType) {
        if (transportType != null && !transportType.isBlank()) {
            for (var type : TransportType.values()) {
                if (transportType.equalsIgnoreCase(type.name())) {
                    return Optional.of(type);
                }
            }
        }
        return Optional.empty();
    }
}