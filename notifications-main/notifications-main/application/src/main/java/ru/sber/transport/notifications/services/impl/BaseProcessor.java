package ru.sber.transport.notifications.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import ru.sber.transport.notifications.database.model.Notification;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationSettings;
import ru.sber.transport.notifications.services.NotificationProcessor;
import ru.sber.transport.notifications.services.NotificationService;
import ru.sber.transport.notifications.services.Processor;

import java.util.List;
import java.util.UUID;

/**
 * Общая реализация процессоров.
 *
 * @param <T> тип сущности уведомления.
 */
@Slf4j
@Setter(onMethod_=@Autowired)
abstract class BaseProcessor<T> implements Processor<T> {
    
    private List<NotificationProcessor> processors;
    
    private NotificationService service;

    private ObjectMapper objectMapper;
    
    /**
     * Запустить процессинг.
     *
     * @param receiver получатель уведомления.
     * @param settings настройки.
     * @param entity объект уведомления.
     */
    protected void process(UUID receiver, NotificationSettings settings, T entity) throws JsonProcessingException {
        var notification = new Notification();
        notification.setReceiverId(receiver);
        notification.setSettings(settings);
        notification.setEntity(objectMapper.writeValueAsString(entity));
        var saved = service.save(notification);
    
        log.info("Notification of data {} created", entity);
        
        processors.forEach(processor -> processor.process(saved));
    }
    
}
