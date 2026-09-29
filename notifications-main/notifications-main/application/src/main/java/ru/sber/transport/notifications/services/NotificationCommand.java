package ru.sber.transport.notifications.services;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * Шаблон для команд уведомлений
 * @param <T> модель из БД
 */
public interface NotificationCommand<T> {
    
    /**
     * Будет ли команда выполнена в сравнении предыдущих и текущих данных
     * @param previous аргумент который хранился в базе
     * @param current новое значение аргумента которое будет добавлено в базу
     * @return
     */
    boolean validate(T previous, T current);
    
    /**
     * Отправка уведомления которое удовлетворяет условию validate
     * @param current текущее значение аргумента команды
     */
    void sendNotification(T current) throws JsonProcessingException;
}
