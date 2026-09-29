package ru.sber.transport.driver_track.service.gpsspoofing;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.driver_track.config.GpsSpoofingFilterProperties;

import static org.junit.jupiter.api.Assertions.*;

@UnitTest
@IsolatedTest
@Feature("app_platform_driver_track")
@DisplayName("Проверка политики расчёта радиуса возврата")
class ReturnRadiusPolicyTest {

    private GpsSpoofingFilterProperties properties;
    private ReturnRadiusPolicy policy;

    @BeforeEach
    void setUp() {
        properties = new GpsSpoofingFilterProperties(
                true,
                5,
                6,
                0.150,
                0.005,
                0.060,
                0.100,
                0.150
        );
        policy = new ReturnRadiusPolicy(properties);
    }

    @Test
    @DisplayName("GPS-BE-007: Низкая скорость < 30 км/ч → 0.060 км")
    void lowSpeedIncrement() {
        assertEquals(0.060, policy.selectIncrement(20), 0.0001);
    }

    @Test
    @DisplayName("GPS-BE-007: Средняя скорость 30 км/ч → 0.100 км")
    void mediumSpeedLowBound() {
        assertEquals(0.100, policy.selectIncrement(30), 0.0001);
    }

    @Test
    @DisplayName("GPS-BE-007: Средняя скорость 60 км/ч → 0.100 км")
    void mediumSpeedHighBound() {
        assertEquals(0.100, policy.selectIncrement(60), 0.0001);
    }

    @Test
    @DisplayName("GPS-BE-007: Высокая скорость > 60 км/ч → 0.150 км")
    void highSpeedIncrement() {
        assertEquals(0.150, policy.selectIncrement(80), 0.0001);
    }

    @Test
    @DisplayName("GPS-BE-008: Первый интервал → базовый радиус 0.150 км")
    void firstIntervalBaseRadius() {
        assertEquals(0.150, policy.calculateRadius(1, 100), 0.0001);
    }

    @Test
    @DisplayName("GPS-BE-008: Шесть интервалов, speed 45 км/ч → increment=0.100, R=0.650 км")
    void sixIntervalsIncrement100() {
        assertEquals(0.650, policy.calculateRadius(6, 45), 0.0001);
    }

    @Test
    @DisplayName("GPS-BE-008: Один интервал → 0.150 км независимо от прибавки")
    void oneIntervalAlways150() {
        assertEquals(0.150, policy.calculateRadius(1, 60), 0.0001);
        assertEquals(0.150, policy.calculateRadius(1, 45), 0.0001);
        assertEquals(0.150, policy.calculateRadius(1, 80), 0.0001);
    }

    @Test
    @DisplayName("GPS-BE-008: Радиус растёт с количеством интервалов (speed 45 км/ч → inc=0.100)")
    void radiusGrowsWithIntervals() {
        assertEquals(0.150, policy.calculateRadius(1, 45), 0.0001);  // 0.150 + 0.100*0
        assertEquals(0.250, policy.calculateRadius(2, 45), 0.0001);  // 0.150 + 0.100*1
        assertEquals(0.350, policy.calculateRadius(3, 45), 0.0001);  // 0.150 + 0.100*2
        assertEquals(0.450, policy.calculateRadius(4, 45), 0.0001);  // 0.150 + 0.100*3
    }

    @Test
    @DisplayName("GPS-BE-008: 0 интервалов → базовый радиус")
    void zeroIntervals() {
        assertEquals(0.150, policy.calculateRadius(0, 45), 0.0001);
    }

    @Test
    @DisplayName("Полный расчёт: 3 интервала, скорость 20 км/ч → increment=0.060")
    void fullCalculationLowSpeed() {
        assertEquals(0.270, policy.calculateRadius(3, 20), 0.0001);
    }

    @Test
    @DisplayName("Полный расчёт: 4 интервала, скорость 45 км/ч → increment=0.100")
    void fullCalculationMediumSpeed() {
        assertEquals(0.450, policy.calculateRadius(4, 45), 0.0001);
    }

    @Test
    @DisplayName("Полный расчёт: 2 интервала, скорость 70 км/ч → increment=0.150")
    void fullCalculationHighSpeed() {
        assertEquals(0.300, policy.calculateRadius(2, 70), 0.0001);
    }
}
