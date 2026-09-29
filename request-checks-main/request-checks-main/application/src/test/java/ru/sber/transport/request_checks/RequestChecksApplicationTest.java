package ru.sber.transport.request_checks;

import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.transport.postgres.EmbeddedPostgres;

@EmbeddedPostgres
@DisplayName("Проверка запуска")
class RequestChecksApplicationTest {

    @Test
    @DisplayName("Запуск")
    void applicationStartsSuccessfullyWithTestProfile() {
        try {
            RequestChecksApplication.main("--spring.profiles.active=test");
        } catch (Exception e) {
            fail(e);
        }
    }

}