package ru.sber.transport.notifications.database.model.settings.timing;

/**
 * Типы оповещений по времени.
 */
public enum EventType {
    
    /**
     * Во время события.
     */
    AT_EVENT,
    
    /**
     * Перед контрольным сроком.
     */
    BEFORE_DEADLINE
}
