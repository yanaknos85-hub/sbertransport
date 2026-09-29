package ru.sber.transport.audit;

/**
 * Результаты выполнения.
 */
public enum Result {

    /**
     * Успех.
     */
    SUCCESS,

    /**
     * Не авторизован.
     */
    UNAUTHORIZED,

    /**
     * Прочие ошибки.
     */
    FAIL,

    /**
     * Запрещено.
     */
    FORBIDDEN,

    /**
     * Неизвестная операция.
     */
    NOT_FOUND,

    /**
     * Ошибка входных данных.
     */
    USER_ERROR
}
