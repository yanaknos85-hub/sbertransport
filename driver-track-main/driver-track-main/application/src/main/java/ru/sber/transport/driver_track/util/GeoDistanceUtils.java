package ru.sber.transport.driver_track.util;

import lombok.experimental.UtilityClass;
import ru.sber.transport.driver_track.database.driver_track.tables.records.CoordinateRecord;

/**
 * Утилитарный класс для расчёта геодезических расстояний между GPS-координатами.
 */
@UtilityClass
public final class GeoDistanceUtils {

    public static final double EARTH_RADIUS_KM = 6371;

    /**
     * Расстояние между двумя GPS-координатами по формуле гаверсинусов (в км).
     */
    public static double calculateDistance(CoordinateRecord coord1, CoordinateRecord coord2) {
        var lat1Rad = Math.toRadians(coord1.getLatitude());
        var lat2Rad = Math.toRadians(coord2.getLatitude());
        var deltaLat = Math.toRadians(coord2.getLatitude() - coord1.getLatitude());
        var deltaLon = Math.toRadians(coord2.getLongitude() - coord1.getLongitude());

        var a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(lat1Rad) * Math.cos(lat2Rad)
                * Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }

    /**
     * Расстояние между двумя GPS-координатами по формуле гаверсинусов (в метрах).
     */
    public static double calculateDistanceMeters(CoordinateRecord coord1, CoordinateRecord coord2) {
        return calculateDistance(coord1, coord2) * 1000;
    }
}
