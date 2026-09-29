package ru.sber.transport.telemechanic.messaging.sender;

import ru.sber.transport.telemechanic.messaging.sender.message.OdometerHistoryValueMessage;

public interface OdometerSender {
    void send(OdometerHistoryValueMessage message);
}
