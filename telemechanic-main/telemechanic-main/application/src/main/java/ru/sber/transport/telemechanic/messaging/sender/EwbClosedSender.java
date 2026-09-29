package ru.sber.transport.telemechanic.messaging.sender;

import ru.sber.transport.telemechanic.messaging.sender.message.EwbClosedMessage;

public interface EwbClosedSender {
    void send(EwbClosedMessage message);
}
