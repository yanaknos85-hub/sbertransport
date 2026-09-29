package ru.sber.transport.notifications.services;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * Процессор обработки данных.
 *
 * @param <T> тип объекта.
 */
public interface Processor<T> {
    
    /**
     * Запустить процессинг.
     *
     * @param data данные.
     */
    void process(T data) throws JsonProcessingException;
    
}
