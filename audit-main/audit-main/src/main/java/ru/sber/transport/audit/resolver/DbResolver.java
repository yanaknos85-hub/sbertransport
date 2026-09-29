package ru.sber.transport.audit.resolver;

import java.time.LocalDateTime;

public interface DbResolver {
    void save(String event, String user, LocalDateTime createdAt, String message);
}
