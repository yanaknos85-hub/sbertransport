package ru.sber.transport.telemechanic.provider;

import ru.sber.transport.dispatcher.messages.AutoparkMessage;
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
    
    /**
     * Привязка подразделения к филиалу контрагента.
     * @param message данные филиала конрагента.
     */
    void addAutopark(AutoparkMessage message);
    
    /**
     * Удаление привязки подраздление
     * @param message
     */
    void deleteAutopark(AutoparkMessage message);
    
}
