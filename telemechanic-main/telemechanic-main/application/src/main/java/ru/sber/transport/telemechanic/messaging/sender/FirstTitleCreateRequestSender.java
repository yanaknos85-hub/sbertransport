package ru.sber.transport.telemechanic.messaging.sender;

import ru.sber.transport.telemechanic.FirstTitleCreateResponseMessage;

public interface FirstTitleCreateRequestSender {
    void send(FirstTitleCreateResponseMessage message);
}
