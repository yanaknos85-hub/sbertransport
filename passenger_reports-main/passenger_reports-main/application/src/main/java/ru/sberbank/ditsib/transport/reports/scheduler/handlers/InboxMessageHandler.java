package ru.sberbank.ditsib.transport.reports.scheduler.handlers;

import ru.sberbank.ditsib.transport.reports.model.InboxMessage;

public interface InboxMessageHandler {
    void handle(InboxMessage message);
    boolean canHandle(InboxMessage message);
}
