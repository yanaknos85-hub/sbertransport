package ru.sber.transport.authentication.business.dto;

/**
 * Возможные результаты.
 */
public enum Result {
    
    /**
     * Неверный пароль.
     */
    WRONG_PASSWORD,
    
    /**
     * Успех.
     */
    SUCCESS,
    
    /**
     * Неверный логин.
     */
    WRONG_LOGIN,

    /**
     * Большое количество неудачных попыток авторизации.
     */
    TOO_MANY_LOGIN_TRIES
}
