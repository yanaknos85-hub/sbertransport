package ru.sberbank.ditsib.transport.gateway;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;

@DisplayName("Проверка запуска")
class GatewayApplicationTest {

    @Test
    @DisplayName("Запуск")
    void test() {
        try {
            GatewayApplication.main();
        } catch (Exception e) {
            fail(e);
        }
    }

}