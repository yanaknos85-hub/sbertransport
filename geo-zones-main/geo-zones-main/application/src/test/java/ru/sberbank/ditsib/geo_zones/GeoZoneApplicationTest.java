package ru.sberbank.ditsib.geo_zones;

import io.qameta.allure.Feature;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.utils.collections.MapUtils;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

@SuppressWarnings("unused")
@UnitTest
@Isolated
@IsolatedTest
@Feature("app_platform_geo_zones")
@EmbeddedPostgres(liquibase = "src/main/resources/db/changelog-master.yml")
@DisplayName("Запуск")
class GeoZoneApplicationTest {
    
    @AfterAll
    static void deleteIndex() {
        try {
            FileUtils.deleteDirectory(new File("./target/index"));
        } catch (Exception ignore) {
        }
    }
    
    @Test
    @DisplayName("Проверка запуска")
    void test_start() {
        try {
            GeoZoneApplication.main("--spring.profiles.active=test");
        } catch(Exception e ){
            fail("Startup failed", e);
        }
    }
    
}