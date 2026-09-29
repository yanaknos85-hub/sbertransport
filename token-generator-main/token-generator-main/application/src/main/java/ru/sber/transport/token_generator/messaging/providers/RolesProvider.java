package ru.sber.transport.token_generator.messaging.providers;

import ru.sber.transport.token_generator.database.token_generator.tables.records.RolesRecord;

import java.util.Optional;

/**
 * Провайдер ролей.
 */
public interface RolesProvider {

    /**
     * Получение роли по коду.
     *
     * @param id код роли.
     * @return роль.
     */
    Optional<RolesRecord> get(String id);

    /**
     * Сохранение роли.
     *
     * @param role роль.
     */
    void save(RolesRecord role);

    /**
     * Удаление роли.
     *
     * @param id код роли.
     */
    void delete(String id);
}
