package ru.sber.transport.token_generator.messaging.providers;

import ru.sber.transport.sudir.messages.AccountMessage;

import java.util.Collection;

/**
 * Провайдер УЗ.
 */
public interface AccountsProvider {

    /**
     * Сохранить УЗ.
     *
     * @param id идентификатор УЗ.
     * @param message сообщение.
     */
    void save(String id, AccountMessage message);

    /**
     * Удалить УЗ.
     *
     * @param id идентификатор УЗ.
     */
    void delete(String id);

    /**
     * Получить список ролей УЗ.
     *
     * @param id идентификатор УЗ.
     * @return список ролей.
     */
    Collection<String> findRoles(String id);
}
