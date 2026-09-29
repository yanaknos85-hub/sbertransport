package ru.sberbank.ditsib.transport.request.messaging.message;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import ru.sber.transport.request.messaging.AddressMessage;

import java.util.UUID;

/**
 * Сообщение об используемом адресе.
 */
@Jacksonized
@SuperBuilder
@Getter
public class AddressUsedMessage extends AddressMessage {
    
    /**
     * Идентификатор сотрудника, использовавшего адрес.
     */
    private final UUID employeeId;
    
    /**
     * Флаг того, что адрес являлся первым в последовательности адресов поездки.
     */
    private final boolean first;
    
}
