package ru.sberbank.ditsib.transport.reports.messaging.listeners.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.transport.messaging.messages.AutoparkMessage;
import ru.sberbank.ditsib.transport.reports.mappers.AutoparkDTOMapper;
import ru.sberbank.ditsib.transport.reports.messaging.listeners.AutoparkListener;
import ru.sberbank.ditsib.transport.reports.service.AutoparkService;

/**
 * Implementation of position listener.
 */
@Slf4j
@Component("autoparksInput")
@RequiredArgsConstructor
public class AutoparkListenerImpl implements AutoparkListener   {
    
    private final AutoparkDTOMapper mapper;
    
    private final AutoparkService service;
    
    @Override
    public void handleAutopark(AutoparkMessage message) {
        if (!message.isDeleted()) {
            service.save(mapper.autoparkMessageToAutopark(message));
        }
    }
}
