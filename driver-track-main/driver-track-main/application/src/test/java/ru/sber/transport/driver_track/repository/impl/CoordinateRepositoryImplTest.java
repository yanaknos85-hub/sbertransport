package ru.sber.transport.driver_track.repository.impl;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jooq.JooqTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.database.config.JooqDatabaseConfig;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.driver_track.database.driver_track.tables.records.CoordinateRecord;
import ru.sber.transport.driver_track.repository.CoordinateRepository;
import ru.sber.transport.postgres.EmbeddedPostgres;


import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@UnitTest
@IsolatedTest
@Feature("app_platform_driver_track")
@EmbeddedPostgres(liquibase = "src/main/resources/db/changelog-master.yml")
@DisplayName("Проверка репозиториая координат")
@Transactional
@JooqTest
@ActiveProfiles("test")
@ContextConfiguration(classes = {CoordinateRepositoryImpl.class, JooqDatabaseConfig.class})
public class CoordinateRepositoryImplTest {

    @Autowired
    private CoordinateRepository coordinateRepository;

    @Test
    @DisplayName("Поиск координат по идентификатору трипа")
    void findCoordinatesByTripIdTest() {
        var coordinate1 = new CoordinateRecord(UUID.randomUUID(), UUID.randomUUID(), Instancio.create(Double.class),
                Instancio.create(Double.class), Instancio.create(LocalDateTime.class).truncatedTo(ChronoUnit.SECONDS));
        var coordinate2 = new CoordinateRecord(UUID.randomUUID(), UUID.randomUUID(), Instancio.create(Double.class),
                Instancio.create(Double.class), Instancio.create(LocalDateTime.class).truncatedTo(ChronoUnit.SECONDS));

        coordinateRepository.save(coordinate1);
        coordinateRepository.save(coordinate2);

        var actualCoordinates = coordinateRepository.findCoordinatesByTripId(coordinate1.getTripId());

        assertEquals(1, actualCoordinates.size());

        var actual = actualCoordinates.getFirst();

        assertEquals(coordinate1.getId(), actual.getId());
        assertEquals(coordinate1.getTripId(), actual.getTripId());
        assertEquals(coordinate1.getLatitude(), actual.getLatitude());
        assertEquals(coordinate1.getLongitude(), actual.getLongitude());
        assertEquals(coordinate1.getTime(), actual.getTime());
    }

    @Test
    @DisplayName("Удаление координат по идентификатору трипа")
    void deleteAllByTripIdTest() {
        var coordinate1 = new CoordinateRecord(UUID.randomUUID(), UUID.randomUUID(), Instancio.create(Double.class),
                Instancio.create(Double.class), Instancio.create(LocalDateTime.class).truncatedTo(ChronoUnit.SECONDS));
        var coordinate2 = new CoordinateRecord(UUID.randomUUID(), UUID.randomUUID(), Instancio.create(Double.class),
                Instancio.create(Double.class), Instancio.create(LocalDateTime.class).truncatedTo(ChronoUnit.SECONDS));

        coordinateRepository.save(coordinate1);
        coordinateRepository.save(coordinate2);

        coordinateRepository.deleteAllByTripId(coordinate1.getTripId());

        var actualCoordinates = coordinateRepository.findAll();

        assertEquals(1, actualCoordinates.size());

        var actual = actualCoordinates.getFirst();

        assertEquals(coordinate2.getId(), actual.getId());
        assertEquals(coordinate2.getTripId(), actual.getTripId());
        assertEquals(coordinate2.getLatitude(), actual.getLatitude());
        assertEquals(coordinate2.getLongitude(), actual.getLongitude());
        assertEquals(coordinate2.getTime(), actual.getTime());
    }

    @Test
    @DisplayName("Удаление координа, соответствующих списку поездок")
    void deleteAllByTripIdListTest(){
        var coordinate1 = new CoordinateRecord(UUID.randomUUID(), UUID.randomUUID(), Instancio.create(Double.class),
                Instancio.create(Double.class), Instancio.create(LocalDateTime.class).truncatedTo(ChronoUnit.SECONDS));
        var coordinate2 = new CoordinateRecord(UUID.randomUUID(), UUID.randomUUID(), Instancio.create(Double.class),
                Instancio.create(Double.class), Instancio.create(LocalDateTime.class).truncatedTo(ChronoUnit.SECONDS));
        var coordinate3 = new CoordinateRecord(UUID.randomUUID(), UUID.randomUUID(), Instancio.create(Double.class),
                Instancio.create(Double.class), Instancio.create(LocalDateTime.class).truncatedTo(ChronoUnit.SECONDS));

        coordinateRepository.save(coordinate1);
        coordinateRepository.save(coordinate2);
        coordinateRepository.save(coordinate3);

        coordinateRepository.deleteAllByTripIdList(List.of(coordinate1.getTripId(), coordinate2.getTripId()));

        var actualCoordinates = coordinateRepository.findAll();

        assertEquals(1, actualCoordinates.size());
        var actual = actualCoordinates.getFirst();

        assertEquals(coordinate3.getId(), actual.getId());
    }

}
