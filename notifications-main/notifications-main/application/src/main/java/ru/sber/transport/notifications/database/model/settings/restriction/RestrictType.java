package ru.sber.transport.notifications.database.model.settings.restriction;

/**
 * Типы ограничений отправки.
 */
public enum RestrictType {
    
    /**
     * Позволить всем.
     */
    ALLOW_ALL,
    
    /**
     * Позволить всем, кроме...
     */
    ALLOW_BUT,
    
    /**
     * Не позволять никому, кроме...
     */
    DENY_BUT,
    
    /**
     * Не позволять никому.
     */
    DENY_ALL
}
