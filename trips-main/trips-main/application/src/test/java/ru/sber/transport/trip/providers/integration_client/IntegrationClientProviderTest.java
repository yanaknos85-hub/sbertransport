package ru.sber.transport.trip.providers.integration_client;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.trip.business.model.IntegrationClient;
import ru.sber.transport.trip.messaging.providers.IntegrationClientProvider;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("Проверка провайдера клиентов интеграции")
@EmbeddedPostgres(liquibase = "src/main/resources/db/changelog-master.yml")
@Transactional
@UnitTest
@IsolatedTest
@Feature("app_platform_trips")
@ActiveProfiles({"test", "kafka", "kafka-ssl"})
class IntegrationClientProviderTest extends KafkaTest {

    @Autowired
    private IntegrationClientProvider integrationClientProvider;

    @DisplayName("Проверка сохранения и получения")
    @Test
    void test_save() {
        var client = new IntegrationClient(UUID.randomUUID(), UUID.randomUUID(), true);
        integrationClientProvider.save(client);
        var saved = integrationClientProvider.get(client.getId()).get();
        assertThat(saved.getId()).isEqualTo(client.getId());
        assertThat(saved.getContractorId()).isEqualTo(client.getContractorId());
        assertThat(saved.isActive()).isEqualTo(client.isActive());
    }

}
