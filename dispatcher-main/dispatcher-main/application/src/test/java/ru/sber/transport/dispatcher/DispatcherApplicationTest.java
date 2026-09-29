package ru.sber.transport.dispatcher;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;

import java.security.Key;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
@IsolatedTest
@Feature("app_platform_dispatcher")
@SpringBootTest(classes = DispatcherApplication.class)
@EmbeddedPostgres
@DisplayName("Проверка запуска")
@MockBean(Key.class)
class DispatcherApplicationTest extends KafkaTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    @DisplayName("Запуск")
    void main() {
        assertThat(applicationContext).isNotNull();
    }
}