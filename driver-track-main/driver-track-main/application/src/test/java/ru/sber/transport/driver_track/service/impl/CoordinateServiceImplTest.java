package ru.sber.transport.driver_track.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.driver_track.database.driver_track.tables.records.CoordinateRecord;
import ru.sber.transport.driver_track.database.driver_track.tables.records.DriverMessageRecord;
import ru.sber.transport.driver_track.dto.BatchCoordinateRequest;
import ru.sber.transport.driver_track.dto.BatchPointDTO;
import ru.sber.transport.driver_track.dto.GeoWaypointDTO;
import ru.sber.transport.driver_track.mapper.CoordinateMapper;
import ru.sber.transport.driver_track.mapper.CoordinateMapperImpl;
import ru.sber.transport.driver_track.repository.CoordinateRepository;
import ru.sber.transport.driver_track.repository.DriverRepository;
import ru.sber.transport.driver_track.service.CoordinateService;
import ru.sber.transport.trip.message.TripMessage;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;

@UnitTest
@IsolatedTest
@Feature("app_platform_driver_track")
@DisplayName("Проверка сервиса координат")
class CoordinateServiceImplTest {
    private final CoordinateRepository coordinateRepository = mock(CoordinateRepository.class);
    private final DriverRepository driverRepository = mock(DriverRepository.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final CoordinateMapper coordinateMapper = new CoordinateMapperImpl();
    private final CoordinateService coordinateService = new CoordinateServiceImpl(driverRepository, coordinateRepository, coordinateMapper);

    @BeforeEach
    void beforeAll() {
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    @DisplayName("Проверка сохранения координат из геосервиса")
    void savePointInfoGeoWaypointDTOTest() {
        var geoWaypointDTO = Instancio.create(GeoWaypointDTO.class);
        var driver = new DriverMessageRecord(UUID.randomUUID(), Instancio.create(Boolean.class),
                true, UUID.randomUUID(), Instancio.create(Boolean.class), true);

        when(driverRepository.getByIdNotNull(any())).thenReturn(driver);

        coordinateService.savePointInfo(geoWaypointDTO, driver.getId());

        var captor = ArgumentCaptor.forClass(CoordinateRecord.class);

        verify(coordinateRepository).save(captor.capture());

        var savedCoordinate = captor.getValue();

        assertEquals(driver.getActiveTripId(), savedCoordinate.getTripId());
        assertEquals(geoWaypointDTO.getLatitude(), savedCoordinate.getLatitude());
        assertEquals(geoWaypointDTO.getLongitude(), savedCoordinate.getLongitude());
    }

    @Test
    @DisplayName("Проверка сохранения координат из геосервиса, если поездка равна null")
    void savePointInfoGeoWaypointDTOTripIsNullTest() {
        var geoWaypointDTO = Instancio.create(GeoWaypointDTO.class);
        var driver = new DriverMessageRecord(UUID.randomUUID(), Instancio.create(Boolean.class),
                true, null, Instancio.create(Boolean.class), true);

        when(driverRepository.getByIdNotNull(any())).thenReturn(driver);

        coordinateService.savePointInfo(geoWaypointDTO, driver.getId());

        verify(coordinateRepository, never()).save(any());
    }

    @Test
    @DisplayName("Проверка сохранения координат из геосервиса, если водитель не онлайн")
    void savePointInfoGeoWaypointDTODriverNotOnlineTest() {
        var geoWaypointDTO = Instancio.create(GeoWaypointDTO.class);
        var driver = new DriverMessageRecord(UUID.randomUUID(), Instancio.create(Boolean.class),
                false, UUID.randomUUID(), Instancio.create(Boolean.class), true);

        when(driverRepository.getByIdNotNull(any())).thenReturn(driver);

        coordinateService.savePointInfo(geoWaypointDTO, driver.getId());

        verify(coordinateRepository, never()).save(any());
    }

    @Test
    @DisplayName("Проверка сохранения координат из поездки")
    void savePointInfoTripMessageTest() {
        double latitude = 55.7558;
        double longitude = 37.6173;
        TripMessage message = TripMessage.builder()
                .id(UUID.randomUUID())
                .type(TripMessage.TripType.PASSENGER)
                .factStartTime(LocalDateTime.now())
                .factEndTime(LocalDateTime.now().plusHours(1))
                .expectedStartTime(LocalDateTime.now())
                .expectedEndTime(LocalDateTime.now().plusHours(2))
                .status("active")
                .additional(new TripMessage.AdditionalData(1, "buisness", java.time.Duration.ofNanos(111), 1.1))
                .contractorId(UUID.randomUUID()).requests(List.of(Map.of("request1", "request2")) )
                .waypoints(List.of(Map.of("latitude", latitude, "longitude", longitude)))
                .digitId(12345L)
                .contractorDigitId(12345L)
                .driverId(UUID.randomUUID())
                .vehicleId(UUID.randomUUID())
                .plannedShiftId(UUID.randomUUID())
                .build();

        coordinateService.savePointInfo(message);

        var captor = ArgumentCaptor.forClass(List.class);

        verify(coordinateRepository).saveAll(captor.capture());

        var savedCoordinate = (CoordinateRecord)captor.getValue().get(0);

        assertEquals(latitude, savedCoordinate.getLatitude());
        assertEquals(longitude, savedCoordinate.getLongitude());
    }

    @Test
    @DisplayName("Проверка сохранения координат из поездки, если waypoints пустой")
    void savePointInfoTripMessageIfWaypointsEmptyTest() {
        double latitude = 55.7558;
        double longitude = 37.6173;
        TripMessage message = TripMessage.builder()
                .id(UUID.randomUUID())
                .type(TripMessage.TripType.PASSENGER)
                .factStartTime(LocalDateTime.now())
                .factEndTime(LocalDateTime.now().plusHours(1))
                .expectedStartTime(LocalDateTime.now())
                .expectedEndTime(LocalDateTime.now().plusHours(2))
                .status("active")
                .additional(new TripMessage.AdditionalData(1, "buisness", java.time.Duration.ofNanos(111), 1.1))
                .contractorId(UUID.randomUUID()).requests(List.of(Map.of("request1", "request2")) )
                .waypoints(List.of())
                .digitId(12345L)
                .contractorDigitId(12345L)
                .driverId(UUID.randomUUID())
                .vehicleId(UUID.randomUUID())
                .plannedShiftId(UUID.randomUUID())
                .build();

        coordinateService.savePointInfo(message);

        verify(coordinateRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("Проверка сохранения координат из поездки, если waypoints равен null")
    void savePointInfoTripMessageIfWaypointsNullTest() {
        double latitude = 55.7558;
        double longitude = 37.6173;
        TripMessage message = TripMessage.builder()
                .id(UUID.randomUUID())
                .type(TripMessage.TripType.PASSENGER)
                .factStartTime(LocalDateTime.now())
                .factEndTime(LocalDateTime.now().plusHours(1))
                .expectedStartTime(LocalDateTime.now())
                .expectedEndTime(LocalDateTime.now().plusHours(2))
                .status("active")
                .additional(new TripMessage.AdditionalData(1, "buisness", java.time.Duration.ofNanos(111), 1.1))
                .contractorId(UUID.randomUUID()).requests(List.of(Map.of("request1", "request2")) )
                .waypoints(null)
                .digitId(12345L)
                .contractorDigitId(12345L)
                .driverId(UUID.randomUUID())
                .vehicleId(UUID.randomUUID())
                .plannedShiftId(UUID.randomUUID())
                .build();

        coordinateService.savePointInfo(message);

        verify(coordinateRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("Проверка пакетного сохранения координат")
    void saveBatchPointsTest() {
        var driverId = UUID.randomUUID();
        var tripId = UUID.randomUUID();
        var now = Instant.now();

        var driver = new DriverMessageRecord(driverId, true, true, tripId, true, true);
        when(driverRepository.getByIdNotNull(driverId)).thenReturn(driver);

        var request = new BatchCoordinateRequest(tripId, List.of(
                new BatchPointDTO(55.7558, 37.6173, now),
                new BatchPointDTO(55.7560, 37.6175, now.plusSeconds(300))
        ));

        coordinateService.saveBatchPoints(request, driverId);

        var captor = ArgumentCaptor.forClass(List.class);
        verify(coordinateRepository).saveAll(captor.capture());

        var savedRecords = (List<CoordinateRecord>) captor.getValue();
        assertEquals(2, savedRecords.size());
        assertEquals(tripId, savedRecords.get(0).getTripId());
        assertEquals(55.7558, savedRecords.get(0).getLatitude());
        assertEquals(37.6173, savedRecords.get(0).getLongitude());
    }

    @Test
    @DisplayName("Проверка пакетного сохранения - невалидные точки пропускаются")
    void saveBatchPointsWithInvalidPointsSkippedTest() {
        var driverId = UUID.randomUUID();
        var tripId = UUID.randomUUID();
        var now = Instant.now();

        var driver = new DriverMessageRecord(driverId, true, true, tripId, true, true);
        when(driverRepository.getByIdNotNull(driverId)).thenReturn(driver);

        var request = new BatchCoordinateRequest(tripId, List.of(
                new BatchPointDTO(55.7558, 37.6173, now),
                new BatchPointDTO(100.0, 37.6175, now),
                new BatchPointDTO(55.7560, null, now),
                new BatchPointDTO(55.7562, 37.6177, null)
        ));

        coordinateService.saveBatchPoints(request, driverId);

        var captor = ArgumentCaptor.forClass(List.class);
        verify(coordinateRepository).saveAll(captor.capture());

        var savedRecords = (List<CoordinateRecord>) captor.getValue();
        assertEquals(1, savedRecords.size());
        assertEquals(55.7558, savedRecords.get(0).getLatitude());
        assertEquals(37.6173, savedRecords.get(0).getLongitude());
    }

    @Test
    @DisplayName("Проверка пакетного сохранения - пустой список точек")
    void saveBatchPointsEmptyPointsTest() {
        var driverId = UUID.randomUUID();
        var tripId = UUID.randomUUID();

        var driver = new DriverMessageRecord(driverId, true, true, tripId, true, true);
        when(driverRepository.getByIdNotNull(driverId)).thenReturn(driver);

        var request = new BatchCoordinateRequest(tripId, List.of());

        coordinateService.saveBatchPoints(request, driverId);

        verify(coordinateRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("Проверка пакетного сохранения - отрицательные координаты валидны")
    void saveBatchPointsNegativeCoordinatesTest() {
        var driverId = UUID.randomUUID();
        var tripId = UUID.randomUUID();
        var now = Instant.now();

        var driver = new DriverMessageRecord(driverId, true, true, tripId, true, true);
        when(driverRepository.getByIdNotNull(driverId)).thenReturn(driver);

        var request = new BatchCoordinateRequest(tripId, List.of(
                new BatchPointDTO(-33.8688, 151.2093, now)
        ));

        coordinateService.saveBatchPoints(request, driverId);

        var captor = ArgumentCaptor.forClass(List.class);
        verify(coordinateRepository).saveAll(captor.capture());

        var savedRecords = (List<CoordinateRecord>) captor.getValue();
        assertEquals(1, savedRecords.size());
        assertEquals(-33.8688, savedRecords.get(0).getLatitude());
        assertEquals(151.2093, savedRecords.get(0).getLongitude());
    }
}