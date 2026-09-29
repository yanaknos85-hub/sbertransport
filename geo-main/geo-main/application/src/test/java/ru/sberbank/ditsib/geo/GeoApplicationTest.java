package ru.sberbank.ditsib.geo;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.postgres.EmbeddedPostgres;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_geo")
@SpringBootTest
@EmbeddedPostgres
@DisplayName("Проверка запуска")
class GeoApplicationTest {

    @Autowired
    private ApplicationContext context;
    
    @Test
    @DisplayName("Запуск")
    void main() {
        assertThat(context).isNotNull();
    }
}