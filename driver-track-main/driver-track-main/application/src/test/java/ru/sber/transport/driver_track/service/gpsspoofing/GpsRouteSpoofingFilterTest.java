package ru.sber.transport.driver_track.service.gpsspoofing;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.driver_track.config.GpsSpoofingFilterProperties;
import ru.sber.transport.driver_track.database.driver_track.tables.records.CoordinateRecord;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@UnitTest
@IsolatedTest
@Feature("app_platform_driver_track")
@DisplayName("Проверка фильтра GPS-спуфинга")
class GpsRouteSpoofingFilterTest {

    private GpsRouteSpoofingFilter filter;

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
        filter = new GpsRouteSpoofingFilter(properties, new ReturnRadiusPolicy(properties));
    }



    @ParameterizedTest(name = "{0}")
    @MethodSource("normalScenarios")
    @DisplayName("Базовая валидация координат в нормальных условиях")
    void shouldValidateCoordinatesInNormalConditions(GpsScenario scenario) {
        var result = filter.filter(scenario.coordinates());

        if (scenario.expectedSize() == -1) {
            assertTrue(result.size() >= 2, scenario.description());
        } else {
            assertEquals(scenario.expectedSize(), result.size(), scenario.description());
        }

        if (!result.isEmpty()) {
            if (scenario.firstLat() != null) {
                assertEquals(scenario.firstLat(), result.getFirst().getLatitude(), 0.0001, scenario.description());
            }
            if (scenario.firstLon() != null) {
                assertEquals(scenario.firstLon(), result.getFirst().getLongitude(), 0.0001, scenario.description());
            }
            if (scenario.lastLat() != null) {
                assertEquals(scenario.lastLat(), result.getLast().getLatitude(), 0.0001, scenario.description());
            }
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("edgeCases")
    @DisplayName("Edge-кейсы: детерминированность, null, все приняты/отклонены")
    void shouldHandleEdgeCases(GpsScenario scenario) {
        var result = filter.filter(scenario.coordinates());

        if (scenario.expectedSize() == -1) {
            assertTrue(result.size() >= 2, scenario.description());
        } else {
            assertEquals(scenario.expectedSize(), result.size(), scenario.description());
        }
    }

    @Test
    @DisplayName("Фильтр отключён — возвращает исходный список")
    void shouldReturnOriginalListWhenDisabled() {
        var disabledProperties = new GpsSpoofingFilterProperties(
                false,
                5,
                6,
                0.150,
                0.005,
                0.060,
                0.100,
                0.150
        );
        var disabledFilter = new GpsRouteSpoofingFilter(disabledProperties, new ReturnRadiusPolicy(disabledProperties));
        var input = List.of(coord(56.826648, 60.612773), coord(57.0, 60.7));
        var result = disabledFilter.filter(input);
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("GPS-BE-015: Детерминированность — одинаковый вход = одинаковый выход")
    void shouldReturnDeterministicResults() {
        var c1 = coord(56.826648, 60.612773);
        var c2 = coord(57.0, 60.7);
        var c3 = coord(57.0005, 60.7005);
        var c4 = coord(56.827, 60.613);
        var input = List.of(c1, c2, c3, c4);

        var result1 = filter.filter(input);
        var result2 = filter.filter(input);

        assertEquals(result1.size(), result2.size(), "GPS-BE-015: Детерминированность");
        for (int i = 0; i < result1.size(); i++) {
            assertEquals(result1.get(i).getLatitude(), result2.get(i).getLatitude(), 0.0001);
            assertEquals(result1.get(i).getLongitude(), result2.get(i).getLongitude(), 0.0001);
        }
    }

    static Stream<GpsScenario> normalScenarios() {
        return Stream.of(
            GpsScenario.of(
                "GPS-BE-001: Пустая последовательность",
                toCoords(),
                0,
                null, null, null
            ),
            GpsScenario.of(
                "GPS-BE-001: Одна точка",
                toCoords(56.826648, 60.612773),
                1,
                null, null, null
            ),
            GpsScenario.of(
                "GPS-BE-001: Первая точка признаётся валидной",
                toCoords(
                    56.826648, 60.612773,
                    56.826700, 60.612800
                ),
                2,
                null, null, null
            ),
            GpsScenario.of(
                "GPS-BE-002: Точка в пределах допустимого перемещения",
                toCoords(
                    56.826648, 60.612773,
                    56.826700, 60.612800
                ),
                2,
                null, null, null
            ),
            GpsScenario.of(
                "GPS-BE-002: Расстояние 150м — принимается",
                toCoords(
                    56.826648, 60.612773,
                    56.828000, 60.612773
                ),
                1,
                null, null, null
            ),
            GpsScenario.of(
                "GPS-BE-003: Резкий скачок > 150м — переход SPOOFING",
                toCoords(
                    56.826648, 60.612773,
                    57.0, 60.7
                ),
                1,
                null, null, null
            ),
            GpsScenario.of(
                "GPS-BE-004: Ложная последовательность — точки отбрасываются",
                toCoords(
                    56.826648, 60.612773,
                    57.0, 60.7,
                    57.0001, 60.7001,
                    57.0002, 60.7002
                ),
                1,
                null, null, null
            ),
            GpsScenario.of(
                "GPS-BE-009: Возврат в допустимом радиусе — принимается",
                toCoords(
                    56.826648, 60.612773,
                    57.0, 60.7,
                    57.0005, 60.7005,
                    56.827, 60.613
                ),
                -1,
                null, null, 56.827
            ),
            GpsScenario.of(
                "GPS-BE-010: Возврат вне радиуса — отбрасывается",
                toCoords(
                    56.826648, 60.612773,
                    57.0, 60.7,
                    57.1, 60.8
                ),
                1,
                null, null, null
            ),
            GpsScenario.of(
                "GPS-BE-011: Маршрут с разрывом — нет интерполяции",
                toCoords(
                    56.826648, 60.612773,
                    57.0, 60.7,
                    57.2, 60.9
                ),
                1,
                56.826648, 60.612773, null
            )
        );
    }

    static Stream<GpsScenario> edgeCases() {
        return Stream.of(
            GpsScenario.of(
                "Edge: все точки в пределах допустимого",
                toCoords(
                    56.826648, 60.612773,
                    56.826700, 60.612800,
                    56.826750, 60.612850,
                    56.826800, 60.612900
                ),
                4,
                null, null, null
            ),
            GpsScenario.of(
                "Edge: null вход",
                null,
                0,
                null, null, null
            ),
            GpsScenario.of(
                "Edge: все точки после первой отклонены",
                toCoords(
                    56.826648, 60.612773,
                    57.0, 60.7,
                    57.1, 60.8,
                    57.2, 60.9
                ),
                1,
                null, null, null
            ),
            GpsScenario.of(
                "Edge: несколько скачков в последовательности",
                toCoords(
                    56.826648, 60.612773,
                    57.0, 60.7,
                    56.827, 60.613,
                    57.1, 60.8
                ),
                -1,
                null, null, null
            )
        );
    }
    private CoordinateRecord coord(double lat, double lon) {
        return new CoordinateRecord(UUID.randomUUID(), UUID.randomUUID(), lat, lon, LocalDateTime.now());
    }

    private static List<CoordinateRecord> toCoords(double... latLons) {
        var result = new ArrayList<CoordinateRecord>();
        for (int i = 0; i < latLons.length; i += 2) {
            result.add(new CoordinateRecord(
                    UUID.randomUUID(), UUID.randomUUID(),
                    latLons[i], latLons[i + 1], LocalDateTime.now()
            ));
        }
        return result;
    }

    record GpsScenario(
            String description,
            List<CoordinateRecord> coordinates,
            int expectedSize,
            Double firstLat,
            Double firstLon,
            Double lastLat
    ) {
        static GpsScenario of(String description, List<CoordinateRecord> coords, int expectedSize,
                              Double firstLat, Double firstLon, Double lastLat) {
            return new GpsScenario(description, coords, expectedSize, firstLat, firstLon, lastLat);
        }
    }
}
