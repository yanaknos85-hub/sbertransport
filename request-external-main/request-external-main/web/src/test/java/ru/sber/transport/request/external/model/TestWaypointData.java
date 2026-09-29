package ru.sber.transport.request.external.model;

import java.math.BigDecimal;
import java.util.UUID;

public record TestWaypointData(
        UUID getId,
        String getCountry,
        String getRegion,
        String getCity,
        String getStreet,
        String getHouse,
        String getBuilding,
        String getStructure,
        BigDecimal getLatitude,
        BigDecimal getLongitude
) implements WaypointData {
}
