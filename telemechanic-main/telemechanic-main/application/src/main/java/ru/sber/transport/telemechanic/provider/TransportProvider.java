package ru.sber.transport.telemechanic.provider;

import ru.sber.transport.telemechanic.messaging.sender.message.TransportMessage;

public interface TransportProvider {
    
    void delete(TransportMessage message);
    
    void save(TransportMessage message);
}