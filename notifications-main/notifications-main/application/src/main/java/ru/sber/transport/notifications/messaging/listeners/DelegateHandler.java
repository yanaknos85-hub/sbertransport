package ru.sber.transport.notifications.messaging.listeners;

import ru.sberbank.ditsib.transport.messaging.messages.DelegateMessage;

public interface DelegateHandler {

    void handle(DelegateMessage message);
}
