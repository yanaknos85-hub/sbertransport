package ru.sber.transport.config

import io.qameta.allure.Feature
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.junit.jupiter.api.parallel.Isolated
import ru.sber.qa.allure.layer.layers.UnitTest
import ru.sber.qa.allure.stage.stages.IsolatedTest
import ru.sber.transport.postgres.EmbeddedPostgres
import java.lang.Exception

@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_config_server")
@EmbeddedPostgres(liquibase = "../_documents/liquibase/changelog-master.yml")
@DisplayName("Проверка запуска сервера конфигурации")
class ApplicationTest {

    @Test
    @DisplayName("Запуск")
    fun `test start`() {
        try {
            main("--spring.profiles.active=jdbc,test")
        } catch (e: Exception) {
            fail(e)
        }
    }

}