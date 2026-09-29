package ru.sber.transport.dispatcher.service;

import jakarta.annotation.Nonnull;

import java.util.UUID;

/**
 * Сервис для создания клиентов для интеграции
 */
public interface IntegrationClientService {

    /**
     * Создание нового клиента для интеграции
     *
     * @param contractorId ид контрагента
     * @param email        почта пользователя
     * @param login        логин ТУЗ
     * @param password     пароль ТУЗ
     */
    void add(@Nonnull UUID contractorId,
             @Nonnull String email,
             @Nonnull String login,
             @Nonnull String password);
}
