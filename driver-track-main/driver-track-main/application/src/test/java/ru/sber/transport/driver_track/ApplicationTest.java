package ru.sber.transport.driver_track;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.geo.grpc.service.GeoServiceGrpc;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.postgres.EmbeddedPostgres;


import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
@IsolatedTest
@Feature("app_platform_driver_track")
@SpringBootTest
@EmbeddedPostgres(liquibase = "src/main/resources/db/changelog-master.yml")
@Transactional
@MockitoBean(types = GeoServiceGrpc.GeoServiceBlockingStub.class)
@DisplayName("Проверка запуска")
@ActiveProfiles("test")
public class ApplicationTest {

    @Autowired
    private ApplicationContext context;

    @Test
    @DisplayName("Запуск")
    void test() {
        assertThat(context).isNotNull();
    }
}
