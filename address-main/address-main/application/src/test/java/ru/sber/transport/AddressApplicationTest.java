package ru.sber.transport;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.postgres.EmbeddedPostgres;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

@UnitTest
@IsolatedTest
@Feature("app_platform_address")
@DisplayName("Проверка запуска")
@EmbeddedPostgres
@Disabled("Требуется переработка")
class AddressApplicationTest {

    @Test
    @DisplayName(("Запуск"))
    void test() {
        try {
            AddressApplication.main("--spring.profiles.active=test,kafka,import", "--embedded.temp=${WORKSPACE:..}/tmp", "--server.port=%s".formatted(new Random().nextInt(10000, 60000)));
        } catch (Exception e) {
            fail(e);
        }
    }

}