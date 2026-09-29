package ru.sberbank.ditsib.transport.srm;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.transport.postgres.EmbeddedPostgres;

import static org.assertj.core.api.Fail.fail;

@UnitTest
@Isolated
@Feature("app_passenger_srm")
@EmbeddedPostgres
@DisplayName("Проверка запуска")
class SrmApplicationTest {

    @Test
    @DisplayName("Запуск")
    void test_start() {
        try {
            SrmApplication.main();
        } catch (RuntimeException e) {
            fail(e);
        }
    }
}