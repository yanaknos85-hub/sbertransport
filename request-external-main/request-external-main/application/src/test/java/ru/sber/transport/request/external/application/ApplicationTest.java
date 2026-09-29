package ru.sber.transport.request.external.application;

import static org.junit.jupiter.api.Assertions.fail;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.postgres.EmbeddedPostgres;

@EmbeddedPostgres
@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_request_external")
@DisplayName("Проверка запуска")
class ApplicationTest {

    @Test
    @DisplayName("Запуск")
    void test() {
        try {
            Application.main("--spring.profiles.active=test");
        } catch (Exception e) {
            fail(e);
        }
    }

}
