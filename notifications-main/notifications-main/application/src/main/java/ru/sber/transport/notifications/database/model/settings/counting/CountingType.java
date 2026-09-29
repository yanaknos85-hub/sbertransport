package ru.sber.transport.notifications.database.model.settings.counting;

/**
 * Типы срабатывания событий по количественной составляющей.
 */
public enum CountingType {
    
    /**
     * При достижении значения.
     */
    EXACT,
    
    /**
     * При достижении остатка значения.
     */
    REMAINS,
    
    /**
     * При достижении определенного процента.
     */
    PERCENT
}
