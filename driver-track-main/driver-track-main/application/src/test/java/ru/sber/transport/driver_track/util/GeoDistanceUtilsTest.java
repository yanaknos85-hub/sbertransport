package ru.sber.transport.driver_track.util;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.driver_track.database.driver_track.tables.records.CoordinateRecord;

import static org.junit.jupiter.api.Assertions.*;

@UnitTest
@IsolatedTest
@Feature("app_platform_driver_track")
@DisplayName("Проверка утилиты геодезических расстояний")
class GeoDistanceUtilsTest {

    @Test
    @DisplayName("Расстояние между одинаковыми координатами равно 0")
    void sameCoordinates() {
        var point1 = new CoordinateRecord(null, null, 56.826648, 60.612773, null);
        var point2 = new CoordinateRecord(null, null, 56.826648, 60.612773, null);

        var distance = GeoDistanceUtils.calculateDistance(point1, point2);
        assertEquals(0, distance, 0.001);
    }

    @Test
    @DisplayName("Расстояние между известными координатами (Минск — Москва)")
    void knownDistanceMinsk() {
        var point1 = new CoordinateRecord(null, null, 53.9006, 27.5592,null);
        var point2 = new CoordinateRecord(null, null, 55.7558, 37.6173, null);

        var distance = GeoDistanceUtils.calculateDistance(point1, point2);
        assertTrue(distance > 650 && distance < 700, "Distance should be ~675 km, got " + distance);
    }

    @Test
    @DisplayName("Расстояние симметрично")
    void symmetricDistance() {
        var pointD11 = new CoordinateRecord(null, null, 56.0, 44.0,null);
        var pointD12 = new CoordinateRecord(null, null, 57.0, 45.0, null);
        var pointD21 = new CoordinateRecord(null, null, 57.0, 45.0,null);
        var pointD22 = new CoordinateRecord(null, null, 56.0, 44.0, null);

        var d1 = GeoDistanceUtils.calculateDistance(pointD11, pointD12);
        var d2 = GeoDistanceUtils.calculateDistance(pointD21, pointD22);
        assertEquals(d1, d2, 0.001);
    }

    @Test
    @DisplayName("Расстояние между ближайшими точками")
    void veryClosePoints() {
        var point1 = new CoordinateRecord(null, null, 56.826648, 60.612773,null);
        var point2 = new CoordinateRecord(null, null, 56.826649, 60.612774, null);

        var distance = GeoDistanceUtils.calculateDistance(point1, point2);
        assertTrue(distance < 0.001, "Distance should be < 1m for nearly identical coordinates");
    }
}
