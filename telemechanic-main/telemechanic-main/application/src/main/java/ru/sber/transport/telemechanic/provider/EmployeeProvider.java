package ru.sber.transport.telemechanic.provider;

import ru.sberbank.ditsib.transport.messaging.messages.EmployeeMessage;

/**
 * Поставщик данных сотрудников.
 */
public interface EmployeeProvider {
    
    /**
     * Удаление сотрудника.
     *
     * @param message данные сотрудника для удаления.
     */
    void delete(EmployeeMessage message);
    
    /**
     * Сохранение сотрудника.
     *
     * @param message данные сотрудника для сохранения.
     */
    void save(EmployeeMessage message);
    
}
