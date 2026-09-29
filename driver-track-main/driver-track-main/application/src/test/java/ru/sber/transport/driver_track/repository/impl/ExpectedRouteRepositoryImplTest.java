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
import ru.sber.transport.driver_track.database.driver_track.tables.records.ExpectedRouteRecord;
import ru.sber.transport.driver_track.dto.RouteSource;
import ru.sber.transport.driver_track.repository.ExpectedRouteRepository;
import ru.sber.transport.geo.grpc.service.GeoServiceGrpc;
import ru.sber.transport.postgres.EmbeddedPostgres;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@UnitTest
@IsolatedTest
@Feature("app_platform_driver_track")
@EmbeddedPostgres(liquibase = "src/main/resources/db/changelog-master.yml")
@SpringBootTest
@DisplayName("Проверка репозитория плановых маршрутов")
@Transactional
@ActiveProfiles("test")
@MockitoBean(types = JwtDecoder.class)
@MockitoBean(types = GeoServiceGrpc.GeoServiceBlockingStub.class)
class ExpectedRouteRepositoryImplTest {
    @Autowired
    private ExpectedRouteRepository expectedRouteRepository;

    @Test
    void findByTripIdAndSourceTest() {
        var route1 = new ExpectedRouteRecord(UUID.randomUUID(), UUID.randomUUID(), JSON.valueOf("{}"), RouteSource.FORMULA.name());
        var route2 = new ExpectedRouteRecord(UUID.randomUUID(), UUID.randomUUID(), JSON.valueOf("{}"), RouteSource.FORMULA.name());

        expectedRouteRepository.save(route1);
        expectedRouteRepository.save(route2);

        var actualOpt = expectedRouteRepository.findByTripIdAndSource(route1.getTripId(), RouteSource.FORMULA);
        assertTrue(actualOpt.isPresent());

        var actual = actualOpt.get();
        assertEquals(route1.getId(), actual.getId());
        assertEquals(route1.getTripId(), actual.getTripId());
        assertEquals(route1.getCoords(), actual.getCoords());
    }

    @Test
    void insertIfNotExistTest(){
        var route1 = new ExpectedRouteRecord(UUID.randomUUID(), UUID.randomUUID(), JSON.valueOf("{}"), RouteSource.FORMULA.name());

        expectedRouteRepository.insertIfNotExist(route1);
        var countRecords = expectedRouteRepository.count();
        assertEquals(1, countRecords);

        var route2 = new ExpectedRouteRecord(UUID.randomUUID(), route1.getTripId(), JSON.valueOf("{}"), RouteSource.FORMULA.name());
        expectedRouteRepository.insertIfNotExist(route2);

        assertEquals(route1.getTripId(), route2.getTripId());
        assertEquals(route1.getSource(), route2.getSource());
        countRecords = expectedRouteRepository.count();
        assertEquals(1, countRecords);
    }
}