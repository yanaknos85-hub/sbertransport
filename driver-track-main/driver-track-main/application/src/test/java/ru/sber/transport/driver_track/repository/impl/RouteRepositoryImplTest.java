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
import ru.sber.transport.driver_track.database.driver_track.tables.records.RouteRecord;
import ru.sber.transport.driver_track.dto.RouteSource;
import ru.sber.transport.driver_track.repository.RouteRepository;
import ru.sber.transport.geo.grpc.service.GeoServiceGrpc;
import ru.sber.transport.postgres.EmbeddedPostgres;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@UnitTest
@IsolatedTest
@Feature("app_platform_driver_track")
@EmbeddedPostgres(liquibase = "src/main/resources/db/changelog-master.yml")
@SpringBootTest
@DisplayName("Проверка репозитория маршрутов")
@Transactional
@ActiveProfiles("test")
@MockitoBean(types = JwtDecoder.class)
@MockitoBean(types = GeoServiceGrpc.GeoServiceBlockingStub.class)
class RouteRepositoryImplTest {

    @Autowired
    private RouteRepository routeRepository;

    @Test
    @DisplayName("Поиск маршрута по идентификатору трипа")
    void findByTripIdTest(){
        var route1 = new RouteRecord(UUID.randomUUID(), UUID.randomUUID(), JSON.valueOf("{}"), RouteSource.FORMULA.name());
        var route2 = new RouteRecord(UUID.randomUUID(), UUID.randomUUID(), JSON.valueOf("{}"), RouteSource.FORMULA.name());

        routeRepository.save(route1);
        routeRepository.save(route2);

        var actualOpt = routeRepository.findByTripIdAndSource(route1.getTripId(), RouteSource.FORMULA);
        assertTrue(actualOpt.isPresent());

        var actual = actualOpt.get();
        assertEquals(route1.getId(), actual.getId());
        assertEquals(route1.getTripId(), actual.getTripId());
        assertEquals(route1.getCoords(), actual.getCoords());
    }

    @Test
    @DisplayName("Добавление маршрута если он не существует")
    void insertIfNotExistTest(){
        var route1 = new RouteRecord(UUID.randomUUID(), UUID.randomUUID(), JSON.valueOf("{}"), RouteSource.FORMULA.name());

        routeRepository.insertIfNotExist(route1);
        var countRecords = routeRepository.count();
        assertEquals(1, countRecords);

        var route2 = new RouteRecord(UUID.randomUUID(), route1.getTripId(), JSON.valueOf("{}"), RouteSource.FORMULA.name());
        routeRepository.insertIfNotExist(route2);

        assertEquals(route1.getTripId(), route2.getTripId());
        assertEquals(route1.getSource(), route2.getSource());
        countRecords = routeRepository.count();
        assertEquals(1, countRecords);
    }
}
