package ru.sber.transport.driver_track.service.gpsspoofing;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.driver_track.config.GpsSpoofingFilterProperties;
import ru.sber.transport.driver_track.database.driver_track.tables.records.CoordinateRecord;
import ru.sber.transport.driver_track.dto.RouteDTO;
import ru.sber.transport.driver_track.dto.RouteSource;
import ru.sber.transport.driver_track.service.GeoClient;
import ru.sber.transport.driver_track.service.impl.CalculateRouteServiceImpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@UnitTest
@IsolatedTest
@Feature("app_platform_driver_track")
@DisplayName("Интеграция фильтра GPS-спуфинга с CalculateRouteService")
class GpsSpoofingFilterIntegrationTest {

    private GeoClient geoClient;
    private GpsRouteSpoofingFilter filter;
    private CalculateRouteServiceImpl calculateRouteService;

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
        geoClient = mock(GeoClient.class);
        calculateRouteService = new CalculateRouteServiceImpl(geoClient, filter);
    }

    private CoordinateRecord coord(double lat, double lon, UUID tripId) {
        return new CoordinateRecord(UUID.randomUUID(), tripId, lat, lon, LocalDateTime.now());
    }

    @Test
    @DisplayName("8.1: Полный пайплайн — очищенный трек передаётся в 2GIS и FORMULA")
    void fullPipeline() {
        var tripId = UUID.randomUUID();
        var coords = List.of(
                coord(56.826648, 60.612773, tripId),
                coord(56.826700, 60.612800, tripId),
                coord(56.826750, 60.612850, tripId),
                coord(56.826800, 60.612900, tripId));

        var expectedRoute = new RouteDTO();
        expectedRoute.setDistance(100.0);
        when(geoClient.recreateRoute(any())).thenReturn(expectedRoute);

        var result = calculateRouteService.calculateRoute(coords, tripId);

        assertEquals(2, result.size());
        var captor = ArgumentCaptor.forClass(List.class);
        verify(geoClient).recreateRoute(captor.capture());
        var capturedCoords = (List<CoordinateRecord>) captor.getValue();
        assertEquals(4, capturedCoords.size());
    }

    @Test
    @DisplayName("8.2: Фильтр отключён — все точки проходят без изменений")
    void filterDisabledAllPointsPass() {
        var properties = new GpsSpoofingFilterProperties(
                false,
                5,
                6,
                0.150,
                0.005,
                0.060,
                0.100,
                0.150
        );
        var disabledFilter = new GpsRouteSpoofingFilter(properties, new ReturnRadiusPolicy(properties));
        var mockGeoClient = mock(GeoClient.class);
        var service = new CalculateRouteServiceImpl(mockGeoClient, disabledFilter);

        var tripId = UUID.randomUUID();
        var coords = List.of(
                coord(56.826648, 60.612773, tripId),
                coord(57.0, 60.7, tripId));

        when(mockGeoClient.recreateRoute(any())).thenReturn(new RouteDTO());

        var result = service.calculateRoute(coords, tripId);

        var captor = ArgumentCaptor.forClass(List.class);
        verify(mockGeoClient).recreateRoute(captor.capture());
        var capturedCoords = (List<CoordinateRecord>) captor.getValue();
        assertEquals(2, capturedCoords.size());
    }

    @Test
    @DisplayName("8.3: Очищенный трек проходит через FORMULA расчёт")
    void cleanedRoutePassesThroughFormula() {
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
        var testFilter = new GpsRouteSpoofingFilter(properties, new ReturnRadiusPolicy(properties));
        var testGeoClient = mock(GeoClient.class);
        var testService = new CalculateRouteServiceImpl(testGeoClient, testFilter);

        var tripId = UUID.randomUUID();
        var coords = List.of(
                coord(56.826648, 60.612773, tripId),
                coord(57.0, 60.7, tripId),
                coord(57.0005, 60.7005, tripId),
                coord(56.827, 60.613, tripId));

        when(testGeoClient.recreateRoute(any())).thenThrow(new RuntimeException("2GIS unavailable"));

        var result = testService.calculateRoute(coords, tripId);

        assertTrue(result.containsKey(RouteSource.FORMULA));
        assertNotNull(result.get(RouteSource.FORMULA).getDistance());
        var formulaDistance = result.get(RouteSource.FORMULA).getDistance();
        assertTrue(formulaDistance > 0, "Distance should be positive");
    }
}
