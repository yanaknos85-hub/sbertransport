package ru.sber.transport.driver_track.repository.impl;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.driver_track.database.driver_track.tables.records.DriverMessageRecord;
import ru.sber.transport.driver_track.repository.DriverRepository;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.geo.grpc.service.GeoServiceGrpc;
import ru.sber.transport.postgres.EmbeddedPostgres;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@UnitTest
@IsolatedTest
@Feature("app_platform_driver_track")
@EmbeddedPostgres(liquibase = "src/main/resources/db/changelog-master.yml")
@SpringBootTest
@MockitoBean(types = GeoServiceGrpc.GeoServiceBlockingStub.class)
@DisplayName("Проверка репозитория водителей")
@Transactional
@ActiveProfiles("test")
public class DriverRepositoryImplTest {

    @Autowired
    private DriverRepository driverRepository;

    @Test
    @DisplayName("Получение водителя")
    void getByIdNotNullTest() {
        var driver = new DriverMessageRecord(UUID.randomUUID(), true, true, UUID.randomUUID(), true, true);
        driverRepository.save(driver);

        var actualDriver = driverRepository.getByIdNotNull(driver.getId());

        assertEquals(driver.getId(), actualDriver.getId());
        assertEquals(driver.getActive(), actualDriver.getActive());
        assertEquals(driver.getOnline(), actualDriver.getOnline());
        assertEquals(driver.getActiveTripId(), actualDriver.getActiveTripId());
        assertEquals(driver.getConsent(), actualDriver.getConsent());
    }

    @Test
    @DisplayName("Получение водителя, не найден")
    void getByIdNotNull_notFoundTest() {
        assertThrows(EntityNotFoundException.class, () -> driverRepository.getByIdNotNull(UUID.randomUUID()));
    }
}
