package ru.sber.transport.contractor.messages;

import ru.sber.transport.messaging.Message;

import java.util.UUID;

/**
 * Сообщение с информацией о роли сотрудника
 * @param id
 * @param role
 */
public record EmployeeRoleMessage(

        /**
         * Идентификатор сотрудника
         */
        UUID id,

        /**
         * Роль сотрудника
         */
        String role
) implements Message<UUID> {
    @Override
    public UUID getId() {
        return id;
    }
}
