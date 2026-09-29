package ru.sberbank.ditsib.transport.vehicle.providers;

import ru.sberbank.ditsib.transport.messaging.messages.DepartmentMessage;

/**
 * Поставщик данных подразделений.
 */
public interface DepartmentProvider {
    
    /**
     * Удаление подразделения.
     *
     * @param message данные подразделения для удаления.
     */
    void delete(DepartmentMessage message);
    
    /**
     * Сохранение подразделения.
     *
     * @param message данные подразделения для сохранения.
     */
    void save(DepartmentMessage message);
    
}
