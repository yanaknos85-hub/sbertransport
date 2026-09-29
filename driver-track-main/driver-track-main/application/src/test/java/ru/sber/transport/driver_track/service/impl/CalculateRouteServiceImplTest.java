package ru.sber.transport.driver_track.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.driver_track.database.driver_track.tables.records.CoordinateRecord;
import ru.sber.transport.driver_track.dto.RouteDTO;
import ru.sber.transport.driver_track.dto.RouteSource;
import ru.sber.transport.driver_track.messaging.sender.TripFactDistanceSender;
import ru.sber.transport.driver_track.repository.CoordinateRepository;
import ru.sber.transport.driver_track.service.GeoClient;
import ru.sber.transport.driver_track.service.gpsspoofing.GpsRouteSpoofingFilter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@UnitTest
@IsolatedTest
@Feature("app_platform_driver_track")
@DisplayName("Проверка сервиса расчета и подготовки координат")
class CalculateRouteServiceImplTest {

    private final CoordinateRepository coordinateRepository = mock(CoordinateRepository.class);
    private final GeoClient twoGisClient = mock(GeoClient.class);
    private final TripFactDistanceSender tripFactDistanceSender = mock(TripFactDistanceSender.class);
    private final GpsRouteSpoofingFilter gpsRouteSpoofingFilter = mock(GpsRouteSpoofingFilter.class);
    private final CalculateRouteServiceImpl calculateRouteService = new CalculateRouteServiceImpl(twoGisClient, gpsRouteSpoofingFilter);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("Проверка расчета маршрута")
    void calculateRouteTest() throws JsonProcessingException {
        var tripId = UUID.randomUUID();
        var coords = List.of(
                new CoordinateRecord(UUID.randomUUID(), tripId, 56.826648, 60.612773, LocalDateTime.now().plusMinutes(1)),
                new CoordinateRecord(UUID.randomUUID(), tripId, 56.826882, 60.604988, LocalDateTime.now().plusMinutes(2)),
                new CoordinateRecord(UUID.randomUUID(), tripId, 56.822473, 60.608843, LocalDateTime.now().plusMinutes(3)),
                new CoordinateRecord(UUID.randomUUID(), tripId, 56.824583, 60.613043, LocalDateTime.now().plusMinutes(4)));

        var expectedRoute = Instancio.create(RouteDTO.class);

        when(coordinateRepository.findCoordinatesByTripId(any())).thenReturn(coords);
        when(twoGisClient.recreateRoute(any())).thenReturn(expectedRoute);
        when(gpsRouteSpoofingFilter.filter(any())).thenReturn(coords);


        Map<RouteSource, RouteDTO> actualRoutes = calculateRouteService.calculateRoute(coords, tripId);

        assertEquals(2, actualRoutes.size());

        RouteDTO routeFormula = actualRoutes.get(RouteSource.FORMULA);
        assertEquals(1.3647850608360885, routeFormula.getDistance());
        assertEquals(1, routeFormula.getSegments().size());
        assertEquals(56.826648, routeFormula.getSegments().get(0).getPoints().get(0).getLatitude());
        assertEquals(60.612773,  routeFormula.getSegments().get(0).getPoints().get(0).getLongitude());

        RouteDTO route2Gis = actualRoutes.get(RouteSource.TWO_GIS);
        assertEquals(expectedRoute.getDistance(), route2Gis.getDistance());
        assertEquals(expectedRoute.getSegments().size(), route2Gis.getSegments().size());
        assertEquals(expectedRoute.getSegments().get(0).getPoints().get(0).getLatitude(), route2Gis.getSegments().get(0).getPoints().get(0).getLatitude());
        assertEquals(expectedRoute.getSegments().get(0).getPoints().get(0).getLongitude(),  route2Gis.getSegments().get(0).getPoints().get(0).getLongitude());

    }

    @Test
    @DisplayName("Проверка подготовки координат")
    void prepareCoordsTest() {
        var tripId = UUID.randomUUID();
        var coords = List.of(
                new CoordinateRecord(UUID.randomUUID(), tripId, 56.826648, 60.612773, LocalDateTime.now().plusMinutes(1)),
                new CoordinateRecord(UUID.randomUUID(), tripId, 56.826882, 60.604988, LocalDateTime.now().plusMinutes(2)),
                new CoordinateRecord(UUID.randomUUID(), tripId, 56.822473, 60.608843, LocalDateTime.now().plusMinutes(3)),
                new CoordinateRecord(UUID.randomUUID(), tripId, 56.824583, 60.613043, LocalDateTime.now().plusMinutes(4)));

        List<CoordinateRecord> coordinateRecordList = calculateRouteService.prepareCoords(coords);

        assertEquals(4, coordinateRecordList.size());
        assertEquals(coords.get(0).getLatitude(), coordinateRecordList.get(0).getLatitude());
        assertEquals(coords.get(0).getLongitude(), coordinateRecordList.get(0).getLongitude());
        assertEquals(coords.get(1).getLatitude(), coordinateRecordList.get(1).getLatitude());
        assertEquals(coords.get(1).getLongitude(), coordinateRecordList.get(1).getLongitude());
        assertNotEquals(coords.get(0).getLatitude(), coordinateRecordList.get(2).getLatitude());
        assertNotEquals(coords.get(0).getLongitude(), coordinateRecordList.get(2).getLongitude());
    }

    @Test
    @DisplayName("Проверка подготовки координат с пустым списком")
    void prepareCoordsEmptyCoordsListTest() {
        List<CoordinateRecord> coords = List.of();
        List<CoordinateRecord> coordinateRecordList = calculateRouteService.prepareCoords(coords);

        assertTrue(coordinateRecordList.isEmpty());
    }

    @Test
    @DisplayName("Проверка подготовки координат с координатами меньше 0,003")
    void prepareCoordsSmallDistanceTest() {
        var tripId = UUID.randomUUID();
        var coords = List.of(
                new CoordinateRecord(UUID.randomUUID(), tripId, 56.826648, 60.612773, LocalDateTime.now().plusMinutes(1)),
                new CoordinateRecord(UUID.randomUUID(), tripId, 56.826882, 60.604988, LocalDateTime.now().plusMinutes(2)),
                new CoordinateRecord(UUID.randomUUID(), tripId, 56.826863, 60.604967, LocalDateTime.now().plusMinutes(3)),
                new CoordinateRecord(UUID.randomUUID(), tripId, 56.824583, 60.613043, LocalDateTime.now().plusMinutes(4)));

        List<CoordinateRecord> coordinateRecordList = calculateRouteService.prepareCoords(coords);

        assertEquals(3, coordinateRecordList.size());
        assertFalse(coordinateRecordList.stream().anyMatch(item -> item.getId() == coords.get(2).getId()));
    }


}