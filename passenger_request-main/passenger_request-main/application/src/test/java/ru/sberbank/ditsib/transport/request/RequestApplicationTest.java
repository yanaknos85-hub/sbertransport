package ru.sberbank.ditsib.transport.request;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;

@UnitTest
@Isolated
@Feature("app_passenger_request")
@SpringBootTest(classes = RequestApplication.class)
@EmbeddedPostgres
@DisplayName("Проверка запуска")
@MockitoBean(types = JwtDecoder.class)
class RequestApplicationTest extends KafkaTest {
    
    @Test
    @DisplayName("Запуск")
    void test() {
    }
}
