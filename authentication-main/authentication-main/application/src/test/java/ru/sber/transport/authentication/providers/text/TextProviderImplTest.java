package ru.sber.transport.authentication.providers.text;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.authentication.business.providers.TextProvider;
import ru.sberbank.ditsib.transport.messaging.messages.UserMessage;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
@IsolatedTest
@Feature("app_platform_authentication")
@DisplayName("Проверка работы провайдера")
class TextProviderImplTest {
    
    private final TextProvider textProvider = new TextProviderImpl();
    
    @Test
    @DisplayName("Получение текста из расположения по-умолчанию")
    void test_textProvider_default() {
        assertThat(textProvider.getResetPasswordEmail(UserMessage.Scope.EMPLOYEE)).isEqualTo("Вы успешно сбросили пароль");
    }
    
    @Test
    @DisplayName("Получение текста из расположения")
    void text_textProvider_nonDefault() {
        ((TextProviderImpl) textProvider).setResetPasswordEmailFile("src/test/resources/nonDefault/text/email/resetPassword.html");
        assertThat(textProvider.getResetPasswordEmail(UserMessage.Scope.EMPLOYEE)).isEqualTo("Вы успешно сбросили пароль, кастомный текст");
    }
}