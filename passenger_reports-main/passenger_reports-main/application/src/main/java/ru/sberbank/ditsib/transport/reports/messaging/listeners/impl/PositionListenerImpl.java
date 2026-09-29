package ru.sberbank.ditsib.transport.reports.messaging.listeners.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.transport.messaging.messages.PositionMessage;
import ru.sberbank.ditsib.transport.reports.mappers.PositionMapper;
import ru.sberbank.ditsib.transport.reports.messaging.listeners.PositionListener;
import ru.sberbank.ditsib.transport.reports.service.PositionService;

/**
 * Implementation of position listener.
 */
@Slf4j
@Component("positionsInput")
@RequiredArgsConstructor
class PositionListenerImpl implements PositionListener   {
    
    private final PositionMapper mapper;
    
    private final PositionService positionService;
    
    @Override
    public void handlePosition(PositionMessage message) {
        if (!message.isDeleted()) {
            positionService.save(mapper.positionMessageToPosition(message));
        }
    }
    
}
