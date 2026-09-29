package ru.sber.transport.driver_track.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.jooq.JSON;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.driver_track.database.driver_track.tables.records.CoordinateRecord;
import ru.sber.transport.driver_track.database.driver_track.tables.records.ExpectedRouteRecord;
import ru.sber.transport.driver_track.database.driver_track.tables.records.ExpectedWaypointsTripRecord;
import ru.sber.transport.driver_track.dto.RouteDTO;
import ru.sber.transport.driver_track.dto.RouteSource;
import ru.sber.transport.driver_track.repository.*;
import ru.sber.transport.driver_track.service.*;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@UnitTest
@IsolatedTest
@Feature("app_platform_driver_track")
@DisplayName("Проверка сервиса планового маршрута")
class ExpectedRouteServiceImplTest {
    @Mock
    private ExpectedWaypointsTripService expectedWaypointsTripService;

    @Mock
    private ExpectedRouteRepository expectedRouteRepository;

    @Mock
    private CoordinateRepository coordinateRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ExpectedRouteServiceImpl expectedRouteService;

    @Mock
    private CalculateRouteService calculateRouteService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    @DisplayName("Получение планового маршрута")
    void getExpectedRouteTest() throws JsonProcessingException {
        UUID tripId = UUID.randomUUID();
        RouteSource sourceType = RouteSource.FORMULA;

        var routeRecord = new ExpectedRouteRecord(
                UUID.randomUUID(),
                tripId,
                JSON.valueOf("{\"distance\":100.5,\"segments\":[]}"),
                "FORMULA"
        );

        when(expectedRouteRepository.findByTripIdAndSource(tripId, sourceType))
                .thenReturn(Optional.of(routeRecord));

        var expectedRouteDTO = Instancio.create(RouteDTO.class);
        when(objectMapper.readValue(anyString(), eq(RouteDTO.class)))
                .thenReturn(expectedRouteDTO);

        RouteDTO result = expectedRouteService.getExpectedRoute(tripId, sourceType);

        assertNotNull(result);
        assertEquals(expectedRouteDTO, result);
        verify(expectedRouteRepository, times(1)).findByTripIdAndSource(tripId, sourceType);
        verify(objectMapper, times(1)).readValue(anyString(), eq(RouteDTO.class));
    }

    @Test
    @DisplayName("Метод getExpectedRoute - обработка ситуации, когда нет записи в БД")
     void getExpectedRouteTestWhenNoRecordReturnsNull() {
        UUID tripId = UUID.randomUUID();
        RouteSource sourceType = RouteSource.FORMULA;

        when(expectedRouteRepository.findByTripIdAndSource(tripId, sourceType))
                .thenReturn(Optional.empty());

        RouteDTO result = expectedRouteService.getExpectedRoute(tripId, sourceType);

        assertNull(result);
        verify(expectedRouteRepository, times(1)).findByTripIdAndSource(tripId, sourceType);
        verifyNoInteractions(objectMapper);
    }

    @Test
    @DisplayName("Тест getExpectedRouteFromGeoService - когда нет данных в БД")
    void getExpectedRouteFromGeoServiceShouldDoNothingIfNoRecordsTest() {
        when(expectedWaypointsTripService.getAllNotHandledRecords()).thenReturn(Collections.emptyList());

        expectedRouteService.createExpectedRoute();

        verify(expectedRouteRepository, never()).saveAll(anyList());
        verify(expectedWaypointsTripService, never()).handleCompliteCreateExpectedRoute(anyList());
        verify(coordinateRepository, never()).deleteAllByTripIdList(anyList());
    }

    @Test
    @DisplayName("Создание планового маршрута и сохранение")
    void createExpectedRouteShouldProcessAndSaveRoutes() {
        UUID tripId = UUID.randomUUID();
        ExpectedWaypointsTripRecord tripRecord = new ExpectedWaypointsTripRecord();
        tripRecord.setTripId(tripId);

        List<ExpectedWaypointsTripRecord> tripRecords = List.of(tripRecord);

        when(expectedWaypointsTripService.getAllNotHandledRecords())
                .thenReturn(tripRecords);

        List<CoordinateRecord> coords = List.of(
                new CoordinateRecord(UUID.randomUUID(), tripId, 55.0, 55.0, LocalDateTime.now()),
                new CoordinateRecord(UUID.randomUUID(), tripId, 44.0, 44.0, LocalDateTime.now())
        );
        when(calculateRouteService.prepareCoords(anyList()))
                .thenReturn(coords);

        expectedRouteService.createExpectedRoute();

        verify(expectedRouteRepository, times(0)).insertIfNotExist(any());
        verify(expectedWaypointsTripService).handleCompliteCreateExpectedRoute(anyList());
    }
    @Test
    @DisplayName("Вычисление координат")
    void calculateCoordinateTest() {
        UUID tripId = UUID.randomUUID();
        ExpectedWaypointsTripRecord tripRecord = new ExpectedWaypointsTripRecord();
        tripRecord.setTripId(tripId);

        List<ExpectedWaypointsTripRecord> tripRecords = List.of(tripRecord);

        when(expectedWaypointsTripService.getAllNotHandledRecords())
                .thenReturn(tripRecords);

        List<CoordinateRecord> coords = List.of();
        when(calculateRouteService.prepareCoords(anyList()))
                .thenReturn(coords);

        expectedRouteService.createExpectedRoute();

        verify(expectedRouteRepository, times(0)).insertIfNotExist(any());
        verify(expectedWaypointsTripService).handleCompliteCreateExpectedRoute(anyList());
    }

    @Test
    @DisplayName("Вычисление с пустыми координатами")
    void calculateEmptyCoordinateTest() throws JsonProcessingException {
        UUID tripId = UUID.randomUUID();
        ExpectedWaypointsTripRecord tripRecord = mock(ExpectedWaypointsTripRecord.class);
        when(tripRecord.getTripId()).thenReturn(tripId);
        JSON jsonWaypoints = JSON.valueOf("""
                [{"id":null,"latitude":55.63217551202085,"longitude":37.59024264466765,"orderingIndex":0,"country":null,"region":null,"city":null,"street":null,"house":null,"building":null,"waitingTime":"PT0S","fullAddress":"Россия, Москва, Москва, микрорайон Северное Чертаново, 8 к832, , ","contact":null,"passengers":[{"type":"BOARDING","firstName":"Юлия","patronymic":"Евгеньевна","phone":"+79853868367"}]},{"id":null,"latitude":55.64843105175612,"longitude":37.60948123428694,"orderingIndex":1,"country":null,"region":null,"city":null,"street":null,"house":null,"building":null,"waitingTime":"PT0S","fullAddress":"Россия, Москва, Москва, Ялтинская улица, 10, , ","contact":null,"passengers":[{"type":"UNBOARDING","firstName":"Юлия","patronymic":"Евгеньевна","phone":"+79853868367"}]}]
                """);
        when(tripRecord.getWaypoints()).thenReturn(jsonWaypoints);

        List<ExpectedWaypointsTripRecord> tripRecords = List.of(tripRecord);

        when(expectedWaypointsTripService.getAllNotHandledRecords())
                .thenReturn(tripRecords);

        when(coordinateRepository.findCoordinatesByTripId(tripId))
                .thenReturn(Collections.emptyList());

        List<CoordinateRecord> coords = List.of();
        when(calculateRouteService.prepareCoords(anyList()))
                .thenReturn(coords);

        Map<String, Object> waypoints = new HashMap<>();
        waypoints.put("latitude", 55.63217551202085);
        waypoints.put("longitude", 37.59024264466765);
        when(objectMapper.readValue(
                eq(jsonWaypoints.data()),
                any(TypeReference.class))
        ).thenReturn(List.of(waypoints));

        expectedRouteService.createExpectedRoute();

        verify(expectedRouteRepository, times(0)).insertIfNotExist(any());
        verify(expectedWaypointsTripService).handleCompliteCreateExpectedRoute(anyList());
    }

    @Test
    @DisplayName("Вычисление с пустым waypoints ")
    void calculateCoordinateWaypointsNullTest() throws JsonProcessingException {
        UUID tripId = UUID.randomUUID();
        ExpectedWaypointsTripRecord tripRecord = mock(ExpectedWaypointsTripRecord.class);
        when(tripRecord.getTripId()).thenReturn(tripId);
        JSON jsonWaypoints = null;
        when(tripRecord.getWaypoints()).thenReturn(jsonWaypoints);

        List<ExpectedWaypointsTripRecord> tripRecords = List.of(tripRecord);

        when(expectedWaypointsTripService.getAllNotHandledRecords())
                .thenReturn(tripRecords);

        when(coordinateRepository.findCoordinatesByTripId(tripId))
                .thenReturn(Collections.emptyList());

        List<CoordinateRecord> coords = List.of();
        when(calculateRouteService.prepareCoords(anyList()))
                .thenReturn(coords);

        expectedRouteService.createExpectedRoute();

        verify(expectedRouteRepository, times(0)).insertIfNotExist(any());
        verify(expectedWaypointsTripService).handleCompliteCreateExpectedRoute(anyList());
    }
}