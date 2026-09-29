package ru.sberbank.transport.oto.cargo.messaging.listeners;

import ru.sberbank.ditsib.transport.request.messaging.CargoRequestMessage;
import ru.sberbank.transport.oto.cargo.messaging.messages.RequestMessage;

import java.util.UUID;

/**
 * Обработчик изменений по заявкам.
 */
public interface TripRequestListener {
    
    /**
     * Обработка обычной заявки.
     * @param requestId ID заявки
     * @param message заявка.
     */
    void handle(UUID requestId, RequestMessage message);
    
    /**
     * Обработка грузовой заявки.
     * @param requestId ID заявки
     * @param message заявка.
     */
    void handleCargo(UUID requestId, CargoRequestMessage message);
    
}
