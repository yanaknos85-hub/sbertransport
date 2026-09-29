package ru.sberbank.ditsib.transport.tariff.messaging.listener;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.constants.TaxiClass;
import ru.sberbank.ditsib.transport.messaging.messages.PositionMessage;
import ru.sberbank.ditsib.transport.tariff.database.dao.PositionRepository;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Isolated
@UnitTest
@Feature("app_passenger_tariff")
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@SpringBootTest
@MockitoBean(types = JwtDecoder.class)
@EmbeddedPostgres
@DisplayName("Проверка получения позиций сотрудников")
@ActiveProfiles({"test", "kafka"})
public class PositionListenerTest extends KafkaTest {

    @Autowired
    private PositionRepository repository;

    @AfterEach
    void dropRepository() {
        repository.deleteAllInBatch();
    }

    @Test
    @DisplayName("Проверка получения новой позиции")
    @Transactional
    void handlePosition() {
        var id = UUID.randomUUID();
        var message = PositionMessage.builder()
                .id(id)
                .positionName("Позиция")
                .deleted(false)
                .organizationId(UUID.randomUUID())
                .availableClasses(Set.of(TaxiClass.ECONOMY.name()))
                .build();

        assertThat(repository.count()).isZero();

        produceMessage("service.organization.position", message);

        assertThat(repository.count()).isEqualTo(1);
        var actual = repository.findAll().getFirst();
        assertThat(actual.getId()).isEqualTo(id);
        assertThat(actual.getOrganizationId()).isEqualTo(message.getOrganizationId());
        assertThat(actual.isActive()).isTrue();
        assertThat(actual.getAvailableClasses().contains(TaxiClass.ECONOMY)).isTrue();

        var deleteMessage = PositionMessage.builder()
                .id(id)
                .positionName("Позиция")
                .deleted(true)
                .organizationId(UUID.randomUUID())
                .availableClasses(Set.of(TaxiClass.ECONOMY.name()))
                .build();

        produceMessage("service.organization.position", deleteMessage);

        assertThat(repository.count()).isEqualTo(0);
    }
}
