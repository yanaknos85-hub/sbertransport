package ru.sberbank.ditsib.transport.request.messaging.listeners.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.transport.request.messaging.message.DeadlineSettingsMessage;
import ru.sberbank.ditsib.transport.request.database.model.deadline.DeadlineSettings;
import ru.sberbank.ditsib.transport.request.mappers.DeadlineSettingsMapper;
import ru.sberbank.ditsib.transport.request.service.DeadlineSettingsService;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Реализация слушателя делегатов.
 */
@RequiredArgsConstructor
@Slf4j
public class DeadlineSettingsListenerImpl implements Consumer<Message<DeadlineSettingsMessage>> {
    
    private final DeadlineSettingsService settingsService;
    private final DeadlineSettingsMapper mapper;
    
    public void accept(Message<DeadlineSettingsMessage> message) {
        handle(message.getPayload());
    }
    
    private void handle(DeadlineSettingsMessage message) {
        // использовать hard delete для настроек
        if (message.isDeleted()) {
            Optional<DeadlineSettings> optional = settingsService.getOptional(message.getId());
            optional.ifPresentOrElse(settings -> {
                settingsService.hardDelete(settings);
                log.info("Настройки КС ID '{}' были удалены", message.getId());
            }, () -> {
                log.info("При попытке удаления Настроек КС ID '{}', они не были найдены в БД", message.getId());
                throw new EntityNotFoundException(DeadlineSettings.class, message.getId());
            });
        } else {
            settingsService.save(mapper.fromMessage(message));
            log.info("Настройки КС ID '{}' были записаны / отредактированы", message.getId());
        }
    }
}
