package ru.sber.transport.authentication.business.providers;

import ru.sberbank.ditsib.transport.messaging.messages.UserMessage;

/**
 * Провайдер текста.
 */
public interface TextProvider {
    
    /**
     * Получение текстовки сброса пароля.
     *
     * @return текстовка сброса пароля.
     */
    String getResetPasswordEmail(UserMessage.Scope scope);

    /**
     * Получение текстовки сброса пароля (V2).
     *
     * @return текстовка сброса пароля.
     */
    String getResetPasswordEmailV2();

    /**
     * Получение текстовки подтверждения почты.
     *
     * @return текстовка подтверждения почты.
     */
    String getEmailConfirmation();

    /**
     * Получение текстовки второго фактора авторизации.
     * @return текст.
     */
    String getSecondFactorEmailText();
}
