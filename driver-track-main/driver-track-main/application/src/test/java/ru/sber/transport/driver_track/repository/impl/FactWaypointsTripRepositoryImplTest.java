package ru.sber.transport.driver_track.repository.impl;

import io.qameta.allure.Feature;
import org.jooq.JSON;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.driver_track.database.driver_track.tables.records.FactWaypointsTripRecord;
import ru.sber.transport.driver_track.database.driver_track.tables.records.RouteRecord;
import ru.sber.transport.driver_track.repository.FactWaypointsTripRepository;
import ru.sber.transport.geo.grpc.service.GeoServiceGrpc;
import ru.sber.transport.postgres.EmbeddedPostgres;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static ru.sber.transport.driver_track.dto.RouteSource.FORMULA;
import static ru.sber.transport.driver_track.dto.RouteSource.TWO_GIS;

@UnitTest
@IsolatedTest
@Feature("app_platform_driver_track")
@EmbeddedPostgres(liquibase = "src/main/resources/db/changelog-master.yml")
@SpringBootTest
@DisplayName("Проверка репозитория плановых точек маршрута")
@Transactional
@ActiveProfiles("test")
@MockitoBean(types = JwtDecoder.class)
@MockitoBean(types = GeoServiceGrpc.GeoServiceBlockingStub.class)
class FactWaypointsTripRepositoryImplTest {

    @Autowired
    private FactWaypointsTripRepository factWaypointsTripRepository;

    @Test
    @DisplayName("Получение маршрута по id")
    void findByTripIdTest() {
        var route1 = new FactWaypointsTripRecord(UUID.randomUUID(), UUID.randomUUID(), JSON.valueOf("{}"), false,0, null);
        var route2 = new FactWaypointsTripRecord(UUID.randomUUID(), UUID.randomUUID(), JSON.valueOf("{}"), false,0, null);

        factWaypointsTripRepository.save(route1);
        factWaypointsTripRepository.save(route2);

        var actualOpt = factWaypointsTripRepository.findByTripId(route1.getTripId());
        assertTrue(actualOpt.isPresent());

        var actual = actualOpt.get();
        assertEquals(route1.getId(), actual.getId());
        assertEquals(route1.getTripId(), actual.getTripId());
        assertEquals(route1.getIsTrackCreated(), actual.getIsTrackCreated());
    }

    @Test
    @DisplayName("Получение всех необработанных маршрутов")
    void getAllNotHandledRecordsTest() {
        var route1 = new FactWaypointsTripRecord(UUID.randomUUID(), UUID.randomUUID(), JSON.valueOf("{}"), false,0, null);
        var route2 = new FactWaypointsTripRecord(UUID.randomUUID(), UUID.randomUUID(), JSON.valueOf("{}"), false,0, null);
        var route3 = new FactWaypointsTripRecord(UUID.randomUUID(), UUID.randomUUID(), JSON.valueOf("{}"), true,0, null);

        factWaypointsTripRepository.save(route1);
        factWaypointsTripRepository.save(route3);
        factWaypointsTripRepository.save(route2);


        var actualOpt = factWaypointsTripRepository.getAllNotHandledRecords();
        assertEquals(2, actualOpt.size());
        assertEquals(route1.getId(), actualOpt.get(0).getId());
        assertEquals(route2.getId(), actualOpt.get(1).getId());

        assertEquals(false, actualOpt.get(0).getIsTrackCreated());
        assertEquals(false, actualOpt.get(1).getIsTrackCreated());
    }

    @Test
    @DisplayName("Проставление всем маршрутам флага isTrackCreated")
    void setAllFlagCreatedExpectedRouteTest() {
        var routeFact1 = new FactWaypointsTripRecord(UUID.randomUUID(), UUID.randomUUID(), JSON.valueOf("{}"), false,0, null);
        var routeFact2 = new FactWaypointsTripRecord(UUID.randomUUID(), UUID.randomUUID(), JSON.valueOf("{}"), false,0, null);
        var routeFact3 = new FactWaypointsTripRecord(UUID.randomUUID(), UUID.randomUUID(), JSON.valueOf("{}"), false,0, null);
        factWaypointsTripRepository.saveAll(List.of(routeFact1, routeFact2, routeFact3));

        var routeRecord1 = new RouteRecord(UUID.randomUUID(), routeFact1.getTripId(), JSON.valueOf("{}"), TWO_GIS.name());
        var routeRecord2 = new RouteRecord(UUID.randomUUID(), routeFact2.getTripId(), JSON.valueOf("{}"), TWO_GIS.name());
        var routeRecord3 = new RouteRecord(UUID.randomUUID(), routeFact3.getTripId(), JSON.valueOf("{}"), FORMULA.name());

        factWaypointsTripRepository.handleCompliteCreateExpectedRoute(List.of(routeRecord1, routeRecord2, routeRecord3));

        assertEquals(true, factWaypointsTripRepository.findByTripId(routeFact1.getTripId()).get().getIsTrackCreated());
        assertEquals(true, factWaypointsTripRepository.findByTripId(routeFact2.getTripId()).get().getIsTrackCreated());
        assertEquals(false, factWaypointsTripRepository.findByTripId(routeFact3.getTripId()).get().getIsTrackCreated());
    }

    @Test
    @DisplayName("Проставление всем маршрутам флага isTrackCreated")
    void notSetFlagCreatedExpectedRouteTest() {
        var routeExpected1 = new FactWaypointsTripRecord(UUID.randomUUID(), UUID.randomUUID(), JSON.valueOf("{}"), false,0, null);
        var routeExpected2 = new FactWaypointsTripRecord(UUID.randomUUID(), UUID.randomUUID(), JSON.valueOf("{}"), false,0, null);
        factWaypointsTripRepository.saveAll(List.of(routeExpected1, routeExpected2));

        var routeRecord1 = new RouteRecord(UUID.randomUUID(), routeExpected1.getTripId(), JSON.valueOf("{}"), FORMULA.name());
        var routeRecord2 = new RouteRecord(UUID.randomUUID(), routeExpected2.getTripId(), JSON.valueOf("{}"), FORMULA.name());

        factWaypointsTripRepository.handleCompliteCreateExpectedRoute(List.of(routeRecord1, routeRecord2));

        assertEquals(false, factWaypointsTripRepository.findByTripId(routeExpected1.getTripId()).get().getIsTrackCreated());
        assertEquals(false, factWaypointsTripRepository.findByTripId(routeExpected2.getTripId()).get().getIsTrackCreated());
    }

    @Test
    @DisplayName("Проставление всем маршрутам флага isTrackCreated")
    void SetAllFlagCreatedExpectedRouteTest() {
        var routeExpected1 = new FactWaypointsTripRecord(UUID.randomUUID(), UUID.randomUUID(), JSON.valueOf("{}"), false,0, null);
        var routeExpected2 = new FactWaypointsTripRecord(UUID.randomUUID(), UUID.randomUUID(), JSON.valueOf("{}"), false,0, null);
        factWaypointsTripRepository.saveAll(List.of(routeExpected1, routeExpected2));

        var routeRecord1 = new RouteRecord(UUID.randomUUID(), routeExpected1.getTripId(), JSON.valueOf("{}"), TWO_GIS.name());
        var routeRecord2 = new RouteRecord(UUID.randomUUID(), routeExpected2.getTripId(), JSON.valueOf("{}"), TWO_GIS.name());

        factWaypointsTripRepository.handleCompliteCreateExpectedRoute(List.of(routeRecord1, routeRecord2));

        assertEquals(true, factWaypointsTripRepository.findByTripId(routeExpected1.getTripId()).get().getIsTrackCreated());
        assertEquals(true, factWaypointsTripRepository.findByTripId(routeExpected2.getTripId()).get().getIsTrackCreated());
    }
}