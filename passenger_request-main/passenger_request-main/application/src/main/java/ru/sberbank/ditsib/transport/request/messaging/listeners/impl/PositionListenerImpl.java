package ru.sberbank.ditsib.transport.request.messaging.listeners.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import ru.sberbank.ditsib.transport.request.messaging.message.PositionMessage;
import ru.sberbank.ditsib.transport.request.dto.mapper.EntityDTOMapper;
import ru.sberbank.ditsib.transport.request.service.corp.PositionService;

import java.util.function.Consumer;

/**
 * Implementation of position listener.
 */
@Slf4j
@RequiredArgsConstructor
public class PositionListenerImpl implements Consumer<Message<PositionMessage>> {
    private final EntityDTOMapper mapper;
    
    private final PositionService positionService;
    
    public void accept(Message<PositionMessage> message) {
        handlePosition(message.getPayload());
    }
    
    private void handlePosition(PositionMessage message) {
        if (message.isDeleted()) {
            positionService.get(message.getId()).ifPresent(positionService::delete);
        } else {
            positionService.save(mapper.positionMessageToPosition(message));
        }
    }
    
}
