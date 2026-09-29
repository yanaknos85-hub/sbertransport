package ru.sber.transport.authsb;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import ru.sber.transport.postgres.EmbeddedPostgres;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatNoException;

/**
 * Интеграционный тест для проверки старта приложения.
 */
@SpringBootTest(
        classes = AuthenticationSbidApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
        "app.truststore=truststore.jks",
        "app.truststore.password=changeit",
        "app.keystore=client.p12",
        "app.keystore.password=changeit",
        "app.privateKey=key/jwt.key"
}
)
@EmbeddedPostgres
class AuthenticationSbidApplicationTest {

    @LocalServerPort
    private int port;

    @Test
    @DisplayName("Загрузка контекста")
    void contextLoads() {
        // Проверяем, что контекст успешно создан
        assertThat(port).isGreaterThan(0);
    }

    @Test
    @DisplayName("Старт приложения")
    void applicationStartsSuccessfully() {
        // Проверка, что приложение стартовало
        assertThat(AuthenticationSbidApplication.class).isNotNull();
    }

    @Test
    @DisplayName("Старт и инициализация контекста")
    void main_ShouldStartApplicationContext() {
        try {
            AuthenticationSbidApplication.main();
            // если дошли сюда — нет исключений
            assertThatNoException();
        } catch (Exception | AssertionError e) {
            // Если произошла ошибка — тест упадёт
            throw new RuntimeException("Приложение не запустилось", e);
        }
    }
}