package ru.sberbank.ditsib.transport.role;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.postgres.EmbeddedPostgres;

import static org.assertj.core.api.Assertions.fail;

@UnitTest
@IsolatedTest
@Feature("app_platform_roles")
@EmbeddedPostgres(liquibase = "src/main/resources/db/changelog-master.yml")
@DisplayName("Проверка запуска")
@Disabled("Требуется переработка")
class RoleApplicationTest {
    
    @Test
    @DisplayName("Запуск")
    void test_start() {
        try {
            RoleApplication.main("--spring.profiles.active=test", "--spring.index.ignore=true");
        } catch (Exception e) {
            fail("Start failed", e);
        }
    }

}