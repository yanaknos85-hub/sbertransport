package ru.sberbank.ditsib.transport.database.dao;

import org.junit.jupiter.api.Test;
import org.junit.platform.commons.JUnitException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.request.RequestApplication;
import ru.sberbank.ditsib.transport.request.database.dao.CarLocationTaskRepository;
import ru.sberbank.ditsib.transport.request.database.model.CarLocationTask;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@EmbeddedPostgres
@SpringBootTest(classes = RequestApplication.class)
class CarLocationTaskRepositoryTest extends KafkaTest {

    @Autowired
    private CarLocationTaskRepository carLocationTaskRepository;

    @Test
    @Sql(scripts = {
            "/scripts/cleanup_database.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/request_for_taxi.sql",
            "/scripts/car_location_task.sql"
    })
    void findByOrderPartnerIdAndActiveIsTrue() {
        var carLocationTask = carLocationTaskRepository.findByOrderPartnerIdAndActiveIsTrue("TP-0320-00000484")
                .stream()
                .max(Comparator.comparing(CarLocationTask::getCreatedAt))
                .orElseThrow(() -> new JUnitException("car location task not found"));

        assertThat(carLocationTask)
                .extracting(
                        CarLocationTask::getId,
                        CarLocationTask::getRequestId,
                        CarLocationTask::getOrderPartnerId,
                        CarLocationTask::getContractorId,
                        CarLocationTask::getCreatedAt,
                        CarLocationTask::isActive
                )
                .containsExactly(
                        UUID.fromString("147bba44-e2d6-4b37-9bec-ff433f09e101"),
                        UUID.fromString("7ffd0e5c-b2d9-4c5c-b206-804c34c87617"),
                        "TP-0320-00000484",
                        UUID.fromString("bc5f6b63-a646-4090-9644-2f2abc79d4d4"),
                        LocalDateTime.of(2020, 1, 1, 0, 0, 0),
                        true
                );
    }

    @Test
    @Sql(scripts = {
            "/scripts/cleanup_database.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/request_for_taxi.sql",
            "/scripts/car_location_task.sql"
    })
    void findByRequestIdAndActiveIsTrue() {
        carLocationTaskRepository.deleteById(UUID.fromString("bab2f03e-0995-4fd3-9acc-6a0853a92679"));
        carLocationTaskRepository.flush();
        var carLocationTasks = carLocationTaskRepository.findAllByRequestIdAndActiveIsTrue(UUID.fromString("7ffd0e5c-b2d9-4c5c-b206-804c34c87617"));

        assertThat(carLocationTasks).hasSize(1);
        assertThat(carLocationTasks.get(0))
                .extracting(
                        CarLocationTask::getId,
                        CarLocationTask::getRequestId,
                        CarLocationTask::getOrderPartnerId,
                        CarLocationTask::getContractorId,
                        CarLocationTask::getCreatedAt,
                        CarLocationTask::isActive
                )
                .containsExactly(
                        UUID.fromString("147bba44-e2d6-4b37-9bec-ff433f09e101"),
                        UUID.fromString("7ffd0e5c-b2d9-4c5c-b206-804c34c87617"),
                        "TP-0320-00000484",
                        UUID.fromString("bc5f6b63-a646-4090-9644-2f2abc79d4d4"),
                        LocalDateTime.of(2020, 1, 1, 0, 0, 0),
                        true
                );
    }
}
