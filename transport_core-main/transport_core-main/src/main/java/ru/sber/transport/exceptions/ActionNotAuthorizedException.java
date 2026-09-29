package ru.sber.transport.exceptions;

/**
 * Ошибка возникает при попытке выполнить запрос пользователем с недостаточными правами
 */
public class ActionNotAuthorizedException extends RuntimeException {

    /**
     * Создать исключение.
     *
     * @param e корневое исключение;
     */
    public ActionNotAuthorizedException(Throwable e) {
        super(e);
    }

    /**
     * Создать исключение.
     *
     * @param message сообщение исключения.
     */
    public ActionNotAuthorizedException(String message) {
        super(message);
    }

}