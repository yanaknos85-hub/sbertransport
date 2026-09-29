package ru.sberbank.ditsib.transport.role.messaging.senders;

import ru.sber.transport.roles.database.roles.tables.records.RoleRecord;

/**
 * Отправитель данных о ролях.
 */
public interface RoleSender {
    
    /**
     * Отправить данные о роли.
     *
     * @param entity роль.
     */
    void send(RoleRecord entity, boolean deleted);
    
}
