package ru.sberbank.ditsib.transport.reports.service;

import ru.sberbank.ditsib.transport.reports.model.InboxMessage;

import java.util.List;

public interface InboxMessageService {
    void process(InboxMessage message);
    List<InboxMessage> findAllByStatus(String status);
    int deleteAllOlderThan(int days);
}
