package ru.sberbank.ditsib.transport.vehicle.messaging.sender;

import ru.sberbank.ditsib.transport.vehicle.database.model.Transport;
import ru.sberbank.ditsib.transport.vehicle.messaging.message.TransportMessage;

public interface TransportSender {
    
    void send(TransportMessage message);
    
    void send(Transport transport, boolean isDeleted);
}
