package ru.sber.transport.trips.cargo;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.utils.collections.MapUtils;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EmbeddedPostgres(liquibase = "src/main/resources/db/changelog-master.yml")
@DisplayName("Проверка запуска")
@UnitTest
@IsolatedTest
@Feature("app_platform_trips_cargo")
@Import(MapUtils.class)
@ActiveProfiles({"test", "kafka", "kafka-ssl"})
class TripCargoApplicationTest extends KafkaTest {

    @Autowired
    private ApplicationContext context;

    @Test
    @DisplayName("Запуск")
    void test() {
        assertThat(context).isNotNull();
    }
}