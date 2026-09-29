package ru.sber.transport.driver_track.service.gpsspoofing;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.driver_track.config.GpsSpoofingFilterProperties;
import ru.sber.transport.driver_track.database.driver_track.tables.records.CoordinateRecord;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@UnitTest
@IsolatedTest
@Feature("app_platform_driver_track")
@DisplayName("Проверка окна скорости")
class SpeedWindowTest {

    private SpeedWindow window;

    @BeforeEach
    void setUp() {
        var properties = new GpsSpoofingFilterProperties(
                true,
                5,
                6,
                0.150,
                0.005,
                0.060,
                0.100,
                0.150
        );
        window = new SpeedWindow(properties);
    }

    private CoordinateRecord coord(double lat, double lon) {
        return new CoordinateRecord(UUID.randomUUID(), UUID.randomUUID(), lat, lon, LocalDateTime.now());
    }

    @Test
    @DisplayName("1 интервал: корректный расчёт скорости")
    void oneInterval() {
        window.addPoint(coord(56.0, 44.0));
        window.addPoint(coord(56.001, 44.0)); // ~111м
        assertEquals(1, window.getIntervalCount());
        var kmh = window.calculateAverageSpeedKmh();
        assertTrue(kmh > 75 && kmh < 85, "Speed should be ~80 km/h, got " + kmh);
    }

    @Test
    @DisplayName("Полное окно: 7 точек (6 интервалов)")
    void fullWindow() {
        for (int i = 0; i < 7; i++) {
            window.addPoint(coord(56.0 + i * 0.001, 44.0));
        }
        assertEquals(6, window.getIntervalCount());
        var kmh = window.calculateAverageSpeedKmh();
        assertTrue(kmh > 75 && kmh < 85, "Speed should be ~80 km/h, got " + kmh);
    }

    @Test
    @DisplayName("Более 6 интервалов: обрезается до 7 точек")
    void truncateToSevenPoints() {
        for (int i = 0; i < 10; i++) {
            window.addPoint(coord(56.0 + i * 0.001, 44.0));
        }
        assertEquals(6, window.getIntervalCount()); // Максимум 6
        assertEquals(7, window.getPoints().size());
    }

    @Test
    @DisplayName("Менее 6 интервалов: использует все доступные")
    void lessThanSixIntervals() {
        for (int i = 0; i < 3; i++) {
            window.addPoint(coord(56.0 + i * 0.001, 44.0));
        }
        assertEquals(2, window.getIntervalCount());
    }

    @Test
    @DisplayName("Расчёт скорости в км/ч")
    void speedInKmh() {
        for (int i = 0; i < 5; i++) {
            window.addPoint(coord(56.0 + i * 0.001, 44.0));
        }
        var kmh = window.calculateAverageSpeedKmh();
        assertTrue(kmh > 75 && kmh < 85, "Speed should be ~80 km/h, got " + kmh);
    }
}
