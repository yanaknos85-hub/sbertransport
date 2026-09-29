package ru.sber.transport.push;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.fail;

@UnitTest
@IsolatedTest
@Isolated
@Transactional
@Feature("app_platform_push")
@EmbeddedPostgres
@DisplayName("Проверка запуска")
class PushApplicationTest {

    @DisplayName("Запуск")
    @Test
    void test() {
        try {
            var port = new Random().nextInt(9000, 60000);
            PushApplication.main("--spring.profiles.active=test", "--server.port=%s".formatted(port));
        } catch (Exception e) {
            fail(e);
        }
    }
    
}