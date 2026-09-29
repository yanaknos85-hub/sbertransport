package ru.sber.transport.notifications.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.sber.transport.notifications.database.model.Notification;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationSettings;
import ru.sber.transport.notifications.dto.customer.CustomerDto;
import ru.sber.transport.notifications.services.NotificationProcessor;
import ru.sber.transport.notifications.services.NotificationService;

import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class NotificationGlobalProcessor<T> {
    
    private final List<NotificationProcessor> processors;
    
    private final NotificationService service;

    private final ObjectMapper objectMapper;
    
    /**
     * Запустить процессинг.
     *
     * @param receiver получатель уведомления.
     * @param settings настройки.
     * @param entity объект уведомления.
     */
    public void process(UUID receiver, NotificationSettings settings, T entity) throws JsonProcessingException {
        var notification = new Notification();
        notification.setReceiverId(receiver);
        notification.setSettings(settings);
        notification.setEntity(objectMapper.writeValueAsString(entity));
        var saved = service.save(notification);
        
        log.info(String.format("Notification of data %s created", entity));
        
        processors.forEach(processor -> processor.process(saved));
    }

    /**
     * Запустить процессинг для произвольного получателя.
     *
     * @param receiver получатель уведомления.
     * @param settings настройки.
     * @param entity объект уведомления.
     */
    public void process(UUID receiver, NotificationSettings settings, T entity, CustomerDto customer) throws JsonProcessingException {
        var notification = new Notification();
        notification.setReceiverId(receiver);
        notification.setSettings(settings);
        notification.setCustomer(customer);
        notification.setEntity(objectMapper.writeValueAsString(entity));
        var saved = service.save(notification);

        log.info("Notification of data {} created", entity);

        processors.forEach(processor -> processor.process(saved));
    }
}
