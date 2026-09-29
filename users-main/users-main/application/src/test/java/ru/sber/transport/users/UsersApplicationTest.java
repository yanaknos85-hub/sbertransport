package ru.sber.transport.users;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.UsersApplication;

import static org.junit.jupiter.api.Assertions.fail;

@UnitTest
@IsolatedTest
@Feature("app_platform_users")
@DisplayName("Проверка запуска")
class UsersApplicationTest {

    @Test
    @DisplayName("Запуск")
    void test() {
        try {
            UsersApplication.main("--spring.profiles.active=test");
        } catch (Exception e) {
            fail(e);
        }
    }
    
}