package ru.sberbank.ditsib.transport.tariff.messaging.listener;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.messaging.messages.EmployeeMessage;
import ru.sberbank.ditsib.transport.tariff.database.dao.EmployeeRepository;
import ru.sberbank.ditsib.transport.tariff.database.model.messages.Employee;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Isolated
@UnitTest
@Feature("app_passenger_tariff")
@SpringBootTest
@EmbeddedPostgres
@MockitoBean(types = JwtDecoder.class)
@DisplayName("Проверка получения сотрудников")
@ActiveProfiles({"test", "kafka"})
class EmployeeListenerTest extends KafkaTest {

    @Autowired
    private EmployeeRepository repository;

    @AfterEach
    void dropRepository() {
        repository.deleteAllInBatch();
    }

    @Test
    @DisplayName("Проверка получения нового сотрудника")
    void handleEmployee_new() {
        var id = UUID.randomUUID();
        var message = EmployeeMessage.builder().id(id)
                .email("email@mail.ru")
                .userId(UUID.randomUUID())
                .departmentId(UUID.randomUUID())
                .organizationId(UUID.randomUUID())
                .positionId(UUID.randomUUID())
                .deleted(false)
                .consent(false)
                .firstName("Тест")
                .lastName("Тестов")
                .humanReadableId(Instancio.create(String.class))
                .personnelNumber(Instancio.create(String.class))
                .employeeType(Instancio.create(String.class))
                .build();

        assertThat(repository.count()).isZero();

        produceMessage("service.organization.employee", message);

        assertThat(repository.count()).isEqualTo(1);
        Employee actual = repository.findAll().getFirst();
        assertThat(actual.getId()).isEqualTo(id);
        assertThat(repository.findById(id)).isPresent();
        assertThat(actual.getDepartmentId()).isEqualTo(message.getDepartmentId());
    }

    @Test
    @DisplayName("Удаление")
    void handleEmployee_delete() {
        var id = UUID.randomUUID();
        var message = EmployeeMessage.builder().id(id)
                .email("email@mail.ru")
                .userId(UUID.randomUUID())
                .departmentId(UUID.randomUUID())
                .organizationId(UUID.randomUUID())
                .positionId(UUID.randomUUID())
                .deleted(false)
                .consent(true)
                .firstName("Тест")
                .lastName("Тестов")
                .humanReadableId(Instancio.create(String.class))
                .personnelNumber(Instancio.create(String.class))
                .employeeType(Instancio.create(String.class))
                .build();

        assertThat(repository.count()).isZero();

        produceMessage("service.organization.employee", message);
        assertThat(repository.count()).isEqualTo(1);

        message = EmployeeMessage.builder().id(id)
                .email("email@mail.ru")
                .userId(UUID.randomUUID())
                .departmentId(UUID.randomUUID())
                .organizationId(UUID.randomUUID())
                .positionId(UUID.randomUUID())
                .deleted(true)
                .consent(true)
                .firstName("Тест")
                .lastName("Тестов")
                .humanReadableId(Instancio.create(String.class))
                .personnelNumber(Instancio.create(String.class))
                .employeeType(Instancio.create(String.class))
                .build();
        produceMessage("service.organization.employee", message);

        assertThat(repository.count()).isEqualTo(1);
    }
}
