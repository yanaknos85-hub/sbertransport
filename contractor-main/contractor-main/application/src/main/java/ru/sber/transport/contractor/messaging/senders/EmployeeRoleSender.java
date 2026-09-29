package ru.sber.transport.contractor.messaging.senders;

import java.util.UUID;

/**
 * Отправитель данных о добаленной роли сотрудника.
 */
public interface EmployeeRoleSender {

    /**
     * Отправить данные о добаленной роли сотрудника.
     *
     * @param employeeId id сотрудника.
     * @param role       роль.
     */
    void send(UUID employeeId, String role);
}
