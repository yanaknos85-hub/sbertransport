package ru.sber.transport.authentication.business.dto;

/**
 * Отслеживаемые аудитом действия.
 */
public enum Action {
    
    /**
     * Выход.
     */
    LOGOUT,
    
    /**
     * Повторный вход.
     */
    RELOGIN,
    
    /**
     * Вход.
     */
    LOGIN
}
