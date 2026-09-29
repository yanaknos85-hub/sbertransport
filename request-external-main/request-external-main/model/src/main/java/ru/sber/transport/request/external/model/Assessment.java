package ru.sber.transport.request.external.model;

/**
 * Оценка
 */
public interface Assessment {

    /**
     * Комментарий
     *
     * @return комментарий
     */
    String getComment();

    /**
     * Оценка (0 - 5)
     *
     * @return оценка
     */
    byte getRating();

}
