package ru.sberbank.transport.oto.cargo.messaging.listeners;

import ru.sberbank.ditsib.transport.request.messaging.CargoRequestMultiMessage;

import java.util.UUID;

/**
 * Обработчик изменений по заявкам.
 */
public interface TripRequestMultiListener {
    
    /**
     * Обработка грузовой заявки.
     * @param requestId ID заявки
     * @param message заявка.
     */
    void handleCargo(UUID requestId, CargoRequestMultiMessage message);
    
}