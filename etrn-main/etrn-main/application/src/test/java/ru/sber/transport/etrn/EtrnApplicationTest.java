package ru.sber.transport.etrn;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EmbeddedPostgres
@DisplayName("Проверка запуска")
class EtrnApplicationTest extends KafkaTest {

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    @DisplayName("Запуск")
    void test() {
        assertThat(applicationContext).isNotNull();
        assertThat(applicationContext.isActive()).isTrue();
    }
}
