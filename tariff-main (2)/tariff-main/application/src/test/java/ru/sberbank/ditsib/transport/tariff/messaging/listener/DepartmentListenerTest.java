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
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sberbank.ditsib.transport.messaging.messages.DepartmentMessage;
import ru.sberbank.ditsib.transport.tariff.database.dao.DepartmentRepository;
import ru.sberbank.ditsib.transport.tariff.database.model.messages.Department;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Isolated
@UnitTest
@Feature("app_passenger_tariff")
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@SpringBootTest
@EmbeddedPostgres
@MockitoBean(types = JwtDecoder.class)
@DisplayName("Проверка получения департаментов")
@ActiveProfiles({"test", "kafka"})
class DepartmentListenerTest extends KafkaTest {

    @Autowired
    private DepartmentRepository repository;

    @AfterEach
    void dropRepository() {
        repository.deleteAllInBatch();
    }

    @Test
    @DisplayName("Проверка получения нового департамента")
    void handleDepartment_new() {
        var id = UUID.randomUUID();
        var message = DepartmentMessage.builder()
                .id(id)
                .deleted(false)
                .departmentName("Департамент")
                .organizationId(UUID.randomUUID())
                .humanReadableId(Instancio.create(String.class))
                .code(Instancio.create(String.class))
                .build();

        assertThat(repository.count()).isEqualTo(0);

        produceMessage("service.organization.department", message);

        assertThat(repository.count()).isEqualTo(1);
        Department actual = repository.findAll().getFirst();
        assertThat(actual.getId()).isEqualTo(id);
        assertThat(repository.findById(id).orElse(null)).isNotNull();
        assertThat(actual.getDepartmentName()).isEqualTo(message.getDepartmentName());
        assertThat(actual.getOrganizationId()).isEqualTo(message.getOrganizationId());
    }

    @Test
    @DisplayName("Удаление")
    void handleDepartment_delete() {
        var id = UUID.randomUUID();
        var message = DepartmentMessage.builder().id(id)
                .deleted(false)
                .departmentName("Департамент")
                .organizationId(UUID.randomUUID())
                .humanReadableId(Instancio.create(String.class))
                .code(Instancio.create(String.class))
                .build();

        assertThat(repository.count()).isEqualTo(0);

        produceMessage("service.organization.department", message);
        assertThat(repository.count()).isEqualTo(1);

        message = DepartmentMessage.builder().id(id)
                .deleted(true)
                .departmentName("Департамент")
                .organizationId(UUID.randomUUID())
                .humanReadableId(Instancio.create(String.class))
                .code(Instancio.create(String.class))
                .build();
        produceMessage("service.organization.department", message);

        assertThat(repository.count()).isEqualTo(1);
    }


}
