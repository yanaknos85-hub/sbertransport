package ru.sberbank.transport.oto.cargo;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
@Isolated
@Feature("app_passenger_oto")
@SpringBootTest(properties = "spring.main.cloud-platform=none")
@EmbeddedPostgres
@DisplayName("Проверка запуска")
//@Disabled("Требует актуализации - падает в Jenkins")
@ActiveProfiles("test")
class OtoCargoApplicationTest extends KafkaTest {
    
    @Autowired
    private ApplicationContext applicationContext;
    
    @Test
    @DisplayName("Запуск")
    void test() {
        assertThat(applicationContext).isNotNull();
    }
}