package ru.sber.transport.disp_integration_proxy;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.postgres.EmbeddedPostgres;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
@IsolatedTest
@Feature("app_platform_disp_integration_proxy")
@SpringBootTest
@EmbeddedPostgres
@ActiveProfiles({"test"})
@DisplayName("Проверка запуска")
class DispIntegrationProxyApplicationTest {

    @Autowired
    private ApplicationContext context;

    @Test
    void testApplication() {
        assertThat(context)
                .isNotNull();
    }

}