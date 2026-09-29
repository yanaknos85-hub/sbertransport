package ru.sberbank.ditsib.transport.request.messaging.listeners.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.request.messaging.message.DelegateMessage;
import ru.sberbank.ditsib.transport.request.database.model.corp.Delegate;
import ru.sberbank.ditsib.transport.request.service.DelegateService;

import java.util.function.Consumer;

/**
 * Реализация слушателя делегатов.
 */
@RequiredArgsConstructor
public class DelegateListenerImpl implements Consumer<Message<DelegateMessage>> {
    
    private final DelegateService delegateService;
    
    public void accept(Message<DelegateMessage> message) {
        handle(message.getPayload());
    }
    
    private void handle(DelegateMessage message) {
        var delegate = delegateService.get(message.getId()).orElse(new Delegate());
        if (message.isDeleted()) {
            delegateService.delete(delegate);
        } else {
            delegate.setId(message.getId());
            delegate.setDelegateId(message.getDelegateId());
            delegate.setStartDate(message.getStartDate());
            delegate.setEndDate(message.getEndDate());
            delegate.setSupervisorId(message.getSupervisorId());
            delegate.setTransportType(TransportTypeEnum.fromId(message.getTransportTypeId()).orElse(null));
            delegateService.save(delegate);
        }
    }
}
