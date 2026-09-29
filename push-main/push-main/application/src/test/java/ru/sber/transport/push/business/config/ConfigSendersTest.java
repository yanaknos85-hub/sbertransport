package ru.sber.transport.push.business.config;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_push")
@DisplayName("Проверка конфигурации")
class ConfigSendersTest {

    private final ConfigSenders senders = new ConfigSenders();

    @Test
    @DisplayName("Конфигурация с внутренним файлом")
    void test_config_internalFile() {
        var messaging = senders.configureFirebase("classpath:/firebase.json", "internal file");

        assertThat(messaging).isNotNull();
    }

    @Test
    @DisplayName("Конфигурация с внешним файлом")
    void test_config_externalFile() {
        var messaging = senders.configureFirebase("target/test-classes/firebase.json", "external file");

        assertThat(messaging).isNotNull();
    }

}