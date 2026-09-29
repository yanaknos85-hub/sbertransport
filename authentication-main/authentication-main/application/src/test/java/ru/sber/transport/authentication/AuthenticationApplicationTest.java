package ru.sber.transport.authentication;

import io.qameta.allure.Feature;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.AbstractContextedTest;
import ru.sber.transport.postgres.EmbeddedPostgres;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
@IsolatedTest
@Feature("app_platform_authentication")
@EmbeddedPostgres
@Transactional
@DisplayName("Проверка запуска")
class AuthenticationApplicationTest {

    @Test
    @DisplayName("Запуск")
    void test() {
        AuthenticationApplication.main("--spring.profiles.active=test");
    }

}