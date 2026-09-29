package ru.sber.transport.constants;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enum Тип точки маршрута
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum EventType {
    
    /**
     * Посадка.
     */
    BOARDING,
    
    /**
     * Высадка.
     */
    UNBOARDING,
    
    /**
     * Ожидание.
     */
    WAIT;
}
