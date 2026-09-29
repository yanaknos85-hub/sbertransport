package ru.sberbank.ditsib.transport.request.messaging.senders;

import ru.sberbank.ditsib.transport.request.database.model.Address;

import java.util.UUID;

/**
 * Отправитель адресов.
 */
public interface AddressSender {
    
    /**
     * Отправить данные адреса.
     *
     * @param address данные адреса.
     * @param employeeId идентификатор сотрудника, использующего адрес.
     * @param first флаг, указывающий на то, что адрес первый в списке.
     */
    void send(Address address, UUID employeeId, boolean first);
    
}
